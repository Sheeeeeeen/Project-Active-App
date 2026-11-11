package ph.project.active.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ph.project.active.data.NetworkMonitor
import ph.project.active.data.SignedInAccount
import ph.project.active.domain.AttendanceRepository
import ph.project.active.domain.ClassRepository
import ph.project.active.domain.MembershipRepository
import ph.project.active.domain.StudentSessionStatus
import ph.project.active.domain.StudentSessions
import ph.project.active.domain.TimeSlot
import ph.project.active.domain.UploadState
import kotlin.time.Clock

data class StudentSessionUiRow(
    val className: String,
    val sessionNumber: Int,
    val date: LocalDate,
    val slot: TimeSlot,
    val status: StudentSessionStatus,
    val uploadPending: Boolean,
)

data class StudentSessionsUiState(
    val upcoming: List<StudentSessionUiRow>,
    val today: List<StudentSessionUiRow>,
    val earlier: List<StudentSessionUiRow>,
    val isEmpty: Boolean,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)

class StudentSessionsViewModel(
    account: SignedInAccount,
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    private val attendance: AttendanceRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
    network: NetworkMonitor,
) : ViewModel() {
    private val studentId = account.id
    private val sessions = StudentSessions(classes, memberships, attendance, clock, timeZone)
    private val _state = MutableStateFlow(load(network.status.value.toConnection(), navigation = null))
    val state: StateFlow<StudentSessionsUiState> = _state.asStateFlow()

    init {
        requireStudent(account)
        watchConnection(network, _state) { copy(connection = it) }
    }

    fun refresh() {
        _state.value = load(_state.value.connection, _state.value.navigation)
    }

    fun onNavigationHandled() {
        _state.value = _state.value.copy(navigation = null)
    }

    private fun load(connection: ConnectionUi, navigation: Navigation?): StudentSessionsUiState {
        val today = clock.todayIn(timeZone)
        val rows = sessions(studentId).map { row ->
            val pending = attendance.find(row.session.id, studentId)?.uploadState == UploadState.PENDING
            StudentSessionUiRow(
                className = classes.findClass(row.session.classId)?.name.orEmpty(),
                sessionNumber = row.session.sessionNumber,
                date = row.session.date,
                slot = row.session.slot,
                status = row.status,
                uploadPending = pending,
            )
        }
        val order = compareByDescending<StudentSessionUiRow> { it.date }.thenByDescending { it.slot.start }
        val upcoming = rows.filter { it.date > today }.sortedWith(order)
        val todayRows = rows.filter { it.date == today }.sortedWith(order)
        val earlier = rows.filter { it.date < today }.sortedWith(order)
        return StudentSessionsUiState(
            upcoming = upcoming,
            today = todayRows,
            earlier = earlier,
            isEmpty = rows.isEmpty(),
            connection = connection,
            navigation = navigation,
        )
    }
}
