package ph.project.active.presentation.studentsessions

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import ph.project.active.domain.repository.AttendanceRepository
import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.data.model.SignedInAccount
import ph.project.active.domain.usecase.StudentSessions
import ph.project.active.presentation.common.watchConnection
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.presentation.common.requireStudent
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.toConnection
import ph.project.active.domain.model.UploadState
import ph.project.active.presentation.common.Navigation

class StudentSessionsViewModel(
    account: SignedInAccount,
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    private val attendance: AttendanceRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
    network: NetworkMonitor,
) : ViewModel() {
    init {
        requireStudent(account)
    }

    private val studentId = account.id
    private val sessions = StudentSessions(classes, memberships, attendance, clock, timeZone)
    private val _state = MutableStateFlow(load(network.status.value.toConnection(), navigation = null))
    val state: StateFlow<StudentSessionsUiState> = _state.asStateFlow()

    init {
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
