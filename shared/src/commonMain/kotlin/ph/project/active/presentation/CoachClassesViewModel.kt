package ph.project.active.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DayOfWeek
import ph.project.active.data.NetworkMonitor
import ph.project.active.data.SignedInAccount
import ph.project.active.domain.ClassRepository
import ph.project.active.domain.MembershipRepository
import ph.project.active.domain.MembershipStatus
import ph.project.active.domain.TimeSlot
import ph.project.active.domain.Visibility
import ph.project.active.domain.activeRoster
import ph.project.active.domain.currentStatus

class CoachClassesViewModel(
    account: SignedInAccount,
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    network: NetworkMonitor,
) : ViewModel() {
    init {
        requireCoach(account)
    }

    private val coachId = account.id
    private val _state = MutableStateFlow(load(network.status.value.toConnection(), navigation = null))
    val state: StateFlow<CoachClassesUiState> = _state.asStateFlow()

    init {
        watchConnection(network, _state) { copy(connection = it) }
    }

    fun onCreateClass() {
        _state.update { it.copy(navigation = Navigation.OpenCreateClass) }
    }

    fun onClassTapped(id: String) {
        _state.update { it.copy(navigation = Navigation.OpenManage(id)) }
    }

    fun onNavigationHandled() {
        _state.update { it.copy(navigation = null) }
    }

    private fun load(connection: ConnectionUi, navigation: Navigation?): CoachClassesUiState {
        val rows = classes.allClasses()
            .filter { it.coachId == coachId }
            .map { trxClass ->
                CoachClassItem(
                    classId = trxClass.id,
                    name = trxClass.name,
                    visibility = trxClass.visibility,
                    slotsByWeekday = trxClass.slotsByWeekday,
                    rosterCount = memberships.activeRoster(trxClass.id).size,
                    pendingRequestCount = pendingCount(trxClass.id),
                    isCancelled = trxClass.isCancelled,
                )
            }
        return CoachClassesUiState(
            classes = rows,
            isEmpty = rows.isEmpty(),
            connection = connection,
            navigation = navigation,
        )
    }

    private fun pendingCount(classId: String): Int = memberships.forClass(classId)
        .map { it.studentId }
        .distinct()
        .count { memberships.currentStatus(classId, it) == MembershipStatus.PENDING_REQUEST }
}
