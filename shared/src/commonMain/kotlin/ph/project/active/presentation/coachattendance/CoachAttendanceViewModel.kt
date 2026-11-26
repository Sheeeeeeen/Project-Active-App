package ph.project.active.presentation.coachattendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import ph.project.active.domain.usecase.CoachAttendanceForDate
import ph.project.active.domain.model.CoachAttendanceStatus
import ph.project.active.domain.repository.AttendanceRepository
import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.data.repository.PersonRepository
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.data.model.SignedInAccount
import ph.project.active.presentation.common.watchConnection
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.domain.model.Session
import ph.project.active.domain.model.SessionStatus
import ph.project.active.domain.model.StudentAttendance
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.requireCoach
import ph.project.active.presentation.common.toConnection
import ph.project.active.presentation.common.Navigation
import ph.project.active.domain.repository.sessionsOn
import ph.project.active.presentation.createclass.DayState

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

    /** Looking at "today". An overnight refresh follows the new day; paging back clears this. */
    private var showingToday = true

    private val _state = MutableStateFlow(
        load(clock.todayIn(timeZone), network.status.value.toConnection(), navigation = null),
    )
    val state: StateFlow<CoachAttendanceUiState> = _state.asStateFlow()

    init {
        watchConnection(network, _state) { copy(connection = it) }
        viewModelScope.launch {
            attendance.changes.collect { show(_state.value.date) }
        }
    }

    fun onPreviousDay() {
        showingToday = false
        show(_state.value.date.minus(1, DateTimeUnit.DAY))
    }

    fun onNextDay() {
        val today = clock.todayIn(timeZone)
        val date = _state.value.date
        if (date < today) {
            val next = date.plus(1, DateTimeUnit.DAY)
            showingToday = next == today
            show(next)
        }
    }

    fun refresh() {
        val today = clock.todayIn(timeZone)
        show(if (showingToday) today else _state.value.date)
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
            scheduledSection(section.session, className, section.students)
        }
        val cancelled = classNames.keys.flatMap { classId -> classes.sessionsOn(classId, date) }
            .filter { it.status == SessionStatus.CANCELLED }
            .map { session -> cancelledSection(session, classNames[session.classId].orEmpty()) }
        val sections = (scheduled + cancelled).sortedWith(sectionOrder)
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

    private fun scheduledSection(
        session: Session,
        className: String,
        students: List<StudentAttendance>,
    ): CoachAttendanceSection = CoachAttendanceSection(
        slot = session.slot,
        cancelled = false,
        students = students.map { student -> studentRow(session, className, student) },
        sessionId = session.id,
        className = className,
        sessionNumber = session.sessionNumber,
    )

    private fun cancelledSection(session: Session, className: String): CoachAttendanceSection = CoachAttendanceSection(
        slot = session.slot,
        cancelled = true,
        students = emptyList(),
        sessionId = session.id,
        className = className,
        sessionNumber = session.sessionNumber,
    )

    private fun studentRow(session: Session, className: String, student: StudentAttendance): CoachAttendanceStudentRow {
        val person = people.find(student.studentId)
        val present = student.status == CoachAttendanceStatus.PRESENT
        val record = if (present) attendance.find(session.id, student.studentId) else null
        return CoachAttendanceStudentRow(
            studentId = student.studentId,
            name = person?.name.orEmpty(),
            employeeId = person?.employeeId.orEmpty(),
            status = student.status,
            chosenClassName = if (present) className else null,
            scannedAt = record?.scannedAt,
            scannedTime = record?.scannedAt?.toLocalDateTime(timeZone)?.time,
        )
    }
}

private val sectionOrder = compareBy<CoachAttendanceSection>({ it.slot.start }, { it.cancelled }, { it.className })
