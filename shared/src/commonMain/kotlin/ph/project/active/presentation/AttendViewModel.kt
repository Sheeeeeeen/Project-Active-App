package ph.project.active.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ph.project.active.data.NetworkMonitor
import ph.project.active.data.OutboxSync
import ph.project.active.data.PersonRepository
import ph.project.active.data.SignedInAccount
import ph.project.active.domain.AttendanceRepository
import ph.project.active.domain.AttendanceResult
import ph.project.active.domain.ClassRepository
import ph.project.active.domain.MembershipRepository
import ph.project.active.domain.RecordAttendance
import ph.project.active.domain.Session
import ph.project.active.domain.SessionRef
import ph.project.active.domain.TimeSlot
import ph.project.active.domain.TodaysChoosableSessions
import ph.project.active.domain.UploadState
import kotlin.time.Clock
import kotlin.time.Instant

data class AttendChoice(
    val sessionId: String,
    val className: String,
    val slot: TimeSlot,
    val coachName: String,
)

data class RecordingFor(val className: String, val slot: TimeSlot)

data class RecordedTodayRow(
    val className: String,
    val sessionNumber: Int,
    val scannedAt: Instant,
    val uploadPending: Boolean,
)

sealed interface ScanResult {
    data class Present(
        val className: String,
        val sessionNumber: Int,
        val date: LocalDate,
        val slot: TimeSlot,
        val scannedAt: Instant,
    ) : ScanResult

    data class SavedOffline(
        val className: String,
        val sessionNumber: Int,
        val date: LocalDate,
        val slot: TimeSlot,
        val scannedAt: Instant,
        val uploadPending: Boolean,
    ) : ScanResult

    data class AlreadyRecorded(
        val className: String,
        val sessionNumber: Int,
        val date: LocalDate,
        val firstScannedAt: Instant,
    ) : ScanResult

    data class Rejected(val reason: RejectReason) : ScanResult
}

sealed interface RejectReason {
    data class WrongDate(val codeDate: LocalDate?) : RejectReason
    data object WrongCoach : RejectReason
    data object NotOnRoster : RejectReason
    data object SessionCancelled : RejectReason
    data object ClassCancelled : RejectReason
    data object InvalidCode : RejectReason
    data object SessionUnknown : RejectReason
}

data class AttendUiState(
    val today: LocalDate,
    val sheetOpen: Boolean,
    val choices: List<AttendChoice>,
    val noSessionsToday: Boolean,
    val canScan: Boolean,
    val selectedSessionId: String?,
    val scannerOpen: Boolean,
    val recordingFor: RecordingFor?,
    val result: ScanResult?,
    val recordedToday: List<RecordedTodayRow>,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)

class AttendViewModel(
    account: SignedInAccount,
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    private val attendance: AttendanceRepository,
    private val people: PersonRepository,
    private val recordAttendance: RecordAttendance,
    private val outbox: OutboxSync,
    private val clock: Clock,
    private val timeZone: TimeZone,
    private val network: NetworkMonitor,
) : ViewModel() {
    private val studentId = account.id
    private var chosen: Session? = null
    private val chooser = TodaysChoosableSessions(classes, memberships, clock, timeZone)
    private val _state = MutableStateFlow(initial())
    val state: StateFlow<AttendUiState> = _state.asStateFlow()

    init {
        requireStudent(account)
        watchConnection(network, _state) { copy(connection = it) }
    }

    fun onOpened() {
        _state.update { it.copy(sheetOpen = true) }
    }

    fun onChoose(sessionId: String) {
        val session = sessionById(sessionId) ?: return
        chosen = session
        _state.update { it.copy(selectedSessionId = session.id, canScan = true) }
    }

    fun onCloseSheet() {
        chosen = null
        _state.update {
            it.copy(sheetOpen = false, selectedSessionId = null, canScan = false, scannerOpen = false)
        }
    }

    fun onScanCoachCode() {
        val session = chosen ?: return
        _state.update {
            it.copy(
                scannerOpen = true,
                sheetOpen = false,
                recordingFor = RecordingFor(className(session.classId), session.slot),
            )
        }
    }

    fun onChangeSession() {
        _state.update { it.copy(scannerOpen = false, sheetOpen = true) }
    }

    fun onCodeScanned(code: String) {
        val selected = chosen
        val ref = selected?.let { SessionRef(it.classId, it.date, it.slot) }
        when (val outcome = recordAttendance(studentId, ref, code)) {
            AttendanceResult.NoSessionChosen -> _state.update { it.copy(sheetOpen = true, scannerOpen = false) }
            is AttendanceResult.Recorded -> {
                val session = selected ?: return
                val online = !network.isOffline()
                val result = recordedResult(session, outcome.attendance.scannedAt, online)
                _state.update {
                    it.copy(result = result, scannerOpen = false, recordedToday = recorded(it.today))
                }
                if (online) {
                    viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
                        try {
                            outbox.sync()
                        } catch (_: Exception) {
                        }
                        _state.update { it.copy(recordedToday = recorded(it.today)) }
                    }
                }
            }
            AttendanceResult.AlreadyRecorded -> {
                val session = ref?.let { classes.findSession(it) } ?: return
                val first = attendance.find(session.id, studentId) ?: return
                _state.update {
                    it.copy(
                        result = ScanResult.AlreadyRecorded(
                            className = className(session.classId),
                            sessionNumber = session.sessionNumber,
                            date = session.date,
                            firstScannedAt = first.scannedAt,
                        ),
                        scannerOpen = false,
                    )
                }
            }
            AttendanceResult.SessionUnknown -> reject(RejectReason.SessionUnknown)
            AttendanceResult.ClassCancelled -> reject(RejectReason.ClassCancelled)
            AttendanceResult.SessionCancelled -> reject(RejectReason.SessionCancelled)
            AttendanceResult.NotOnRoster -> reject(RejectReason.NotOnRoster)
            AttendanceResult.InvalidCode -> reject(RejectReason.InvalidCode)
            AttendanceResult.WrongCoach -> reject(RejectReason.WrongCoach)
            AttendanceResult.WrongDate -> reject(RejectReason.WrongDate(parseCodeDate(code)))
        }
    }

    fun onRecordAnother() = resetToSheet()

    fun onChooseDifferentClass() = resetToSheet()

    fun onScanAgain() {
        val session = chosen ?: return
        _state.update {
            it.copy(
                scannerOpen = true,
                sheetOpen = false,
                recordingFor = RecordingFor(className(session.classId), session.slot),
            )
        }
    }

    fun onDone() {
        _state.update { it.copy(result = null) }
    }

    fun refresh() {
        val today = clock.todayIn(timeZone)
        val choices = choices()
        _state.update {
            it.copy(
                today = today,
                choices = choices,
                noSessionsToday = choices.isEmpty(),
                recordedToday = recorded(today),
            )
        }
    }

    fun onNavigationHandled() {
        _state.update { it.copy(navigation = null) }
    }

    private fun initial(): AttendUiState {
        val today = clock.todayIn(timeZone)
        val choices = choices()
        return AttendUiState(
            today = today,
            sheetOpen = false,
            choices = choices,
            noSessionsToday = choices.isEmpty(),
            canScan = false,
            selectedSessionId = null,
            scannerOpen = false,
            recordingFor = null,
            result = null,
            recordedToday = recorded(today),
            connection = network.status.value.toConnection(),
            navigation = null,
        )
    }

    private fun resetToSheet() {
        chosen = null
        _state.update {
            it.copy(
                result = null,
                selectedSessionId = null,
                canScan = false,
                sheetOpen = true,
                scannerOpen = false,
                recordingFor = null,
            )
        }
    }

    private fun reject(reason: RejectReason) {
        _state.update { it.copy(result = ScanResult.Rejected(reason), scannerOpen = false) }
    }

    private fun recordedResult(session: Session, scannedAt: Instant, online: Boolean): ScanResult {
        val name = className(session.classId)
        return if (online) {
            ScanResult.Present(name, session.sessionNumber, session.date, session.slot, scannedAt)
        } else {
            ScanResult.SavedOffline(name, session.sessionNumber, session.date, session.slot, scannedAt, uploadPending = true)
        }
    }

    private fun choices(): List<AttendChoice> = chooser(studentId).map { session ->
        AttendChoice(
            sessionId = session.id,
            className = className(session.classId),
            slot = session.slot,
            coachName = coachName(session.classId),
        )
    }

    private fun recorded(today: LocalDate): List<RecordedTodayRow> = attendance.all()
        .filter { it.studentId == studentId }
        .mapNotNull { record ->
            val session = sessionById(record.sessionId) ?: return@mapNotNull null
            if (session.date != today) return@mapNotNull null
            RecordedTodayRow(
                className = className(session.classId),
                sessionNumber = session.sessionNumber,
                scannedAt = record.scannedAt,
                uploadPending = record.uploadState == UploadState.PENDING,
            )
        }
        .sortedBy { it.scannedAt }

    private fun sessionById(id: String): Session? = classes.allClasses().firstNotNullOfOrNull { trxClass ->
        classes.sessionsOf(trxClass.id).firstOrNull { it.id == id }
    }

    private fun className(classId: String): String = classes.findClass(classId)?.name.orEmpty()

    private fun coachName(classId: String): String {
        val coachId = classes.findClass(classId)?.coachId ?: return ""
        return people.find(coachId)?.name.orEmpty()
    }
}

private val CODE_DATE = Regex("""date=(\d{4}-\d{2}-\d{2})""")

private fun parseCodeDate(code: String): LocalDate? =
    CODE_DATE.find(code)?.groupValues?.getOrNull(1)?.let { text ->
        try {
            LocalDate.parse(text)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
