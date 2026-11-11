package ph.project.active.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import ph.project.active.data.NetworkMonitor
import ph.project.active.data.PersonRepository
import ph.project.active.data.SignedInAccount
import ph.project.active.domain.AttendanceRepository
import ph.project.active.domain.ClassRepository
import ph.project.active.domain.CoachAttendanceForDate
import ph.project.active.domain.CoachAttendanceStatus
import ph.project.active.domain.MembershipRepository
import ph.project.active.domain.SessionStatus
import ph.project.active.domain.TimeSlot
import ph.project.active.domain.sessionsOn
import kotlin.time.Clock
import kotlin.time.Instant

class CoachAttendanceViewModel(
    account: SignedInAccount,
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    private val attendance: AttendanceRepository,
    private val people: PersonRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
    network: NetworkMonitor,
) : ViewModel() {
    init {
        requireCoach(account)
    }

    private val coachId = account.id
    private val forDate = CoachAttendanceForDate(classes, memberships, attendance, clock, timeZone)
    private val _state = MutableStateFlow(
        load(clock.todayIn(timeZone), network.status.value.toConnection(), navigation = null),
    )
    val state: StateFlow<CoachAttendanceUiState> = _state.asStateFlow()

    init {
        watchConnection(network, _state) { copy(connection = it) }
    }

    fun onPreviousDay() = show(_state.value.date.minus(1, DateTimeUnit.DAY))

    fun onNextDay() {
        val today = clock.todayIn(timeZone)
        val date = _state.value.date
        if (date < today) show(date.plus(1, DateTimeUnit.DAY))
    }

    fun onNavigationHandled() {
        _state.value = _state.value.copy(navigation = null)
    }

    private fun show(date: LocalDate) {
        _state.value = load(date, _state.value.connection, _state.value.navigation)
    }

    private fun load(date: LocalDate, connection: ConnectionUi, navigation: Navigation?): CoachAttendanceUiState {
        val today = clock.todayIn(timeZone)
        val classNames = classes.allClasses().filter { it.coachId == coachId }.associate { it.id to it.name }
        val scheduled = forDate(coachId, date).map { section ->
            val className = classNames[section.session.classId].orEmpty()
            CoachAttendanceSection(
                slot = section.session.slot,
                cancelled = false,
                students = section.students.map { student ->
                    val person = people.find(student.studentId)
                    val record = if (student.status == CoachAttendanceStatus.PRESENT) {
                        attendance.find(section.session.id, student.studentId)
                    } else {
                        null
                    }
                    CoachAttendanceStudentRow(
                        studentId = student.studentId,
                        name = person?.name.orEmpty(),
                        employeeId = person?.employeeId.orEmpty(),
                        status = student.status,
                        chosenClassName = if (student.status == CoachAttendanceStatus.PRESENT) className else null,
                        scannedAt = record?.scannedAt,
                    )
                },
            )
        }
        val cancelled = classNames.keys.flatMap { classId -> classes.sessionsOn(classId, date) }
            .filter { it.status == SessionStatus.CANCELLED }
            .map { session ->
                CoachAttendanceSection(slot = session.slot, cancelled = true, students = emptyList())
            }
        val sections = (scheduled + cancelled).sortedWith(compareBy({ it.slot.start }, { it.cancelled }))
        val dayState = if (date < today) DayState.DAY_ENDED else DayState.IN_PROGRESS
        val presentCount = sections.sumOf { section -> section.students.count { it.status == CoachAttendanceStatus.PRESENT } }
        val absentCount = if (dayState == DayState.IN_PROGRESS) {
            0
        } else {
            sections.sumOf { section -> section.students.count { it.status == CoachAttendanceStatus.ABSENT } }
        }
        return CoachAttendanceUiState(
            date = date,
            canGoNext = date < today,
            sections = sections,
            dayState = dayState,
            presentCount = presentCount,
            absentCount = absentCount,
            connection = connection,
            navigation = navigation,
        )
    }
}
