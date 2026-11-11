package ph.project.active.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DayOfWeek
import ph.project.active.data.NetworkMonitor
import ph.project.active.data.PersonRepository
import ph.project.active.data.SignedInAccount
import ph.project.active.domain.AcceptInvite
import ph.project.active.domain.ClassRepository
import ph.project.active.domain.DeclineInvite
import ph.project.active.domain.MembershipRepository
import ph.project.active.domain.MembershipStatus
import ph.project.active.domain.TimeSlot
import ph.project.active.domain.Visibility
import ph.project.active.domain.currentStatus

data class InviteRow(
    val classId: String,
    val className: String,
    val coachName: String,
    val visibility: Visibility,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
)

sealed interface InviteConfirmation {
    data class InviteAccepted(val className: String) : InviteConfirmation
}

data class InvitesUiState(
    val invites: List<InviteRow>,
    val pendingInviteCount: Int,
    val isEmpty: Boolean,
    val confirmation: InviteConfirmation?,
    val error: UiError?,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)

class InvitesViewModel(
    account: SignedInAccount,
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    private val people: PersonRepository,
    private val acceptInvite: AcceptInvite,
    private val declineInvite: DeclineInvite,
    private val network: NetworkMonitor,
) : ViewModel() {
    private val studentId = account.id
    private val _state = MutableStateFlow(
        load(confirmation = null, error = null, navigation = null, connection = network.status.value.toConnection()),
    )
    val state: StateFlow<InvitesUiState> = _state.asStateFlow()

    init {
        requireStudent(account)
        watchConnection(network, _state) { copy(connection = it) }
    }

    fun onAccept(classId: String) {
        if (network.isOffline()) {
            _state.update { it.copy(error = UiError.Offline) }
            return
        }
        acceptInvite(classId = classId, studentId = studentId)
        val name = classes.findClass(classId)?.name.orEmpty()
        _state.value = load(
            confirmation = InviteConfirmation.InviteAccepted(name),
            error = null,
            navigation = _state.value.navigation,
            connection = _state.value.connection,
        )
    }

    fun onDecline(classId: String) {
        if (network.isOffline()) {
            _state.update { it.copy(error = UiError.Offline) }
            return
        }
        declineInvite(classId = classId, studentId = studentId)
        _state.value = load(confirmation = null, error = null, navigation = _state.value.navigation, connection = _state.value.connection)
    }

    fun onNavigationHandled() {
        _state.update { it.copy(navigation = null) }
    }

    private fun load(
        confirmation: InviteConfirmation?,
        error: UiError?,
        navigation: Navigation?,
        connection: ConnectionUi,
    ): InvitesUiState {
        val invites = classes.allClasses()
            .filter { memberships.currentStatus(it.id, studentId) == MembershipStatus.INVITED }
            .map { trxClass ->
                InviteRow(
                    classId = trxClass.id,
                    className = trxClass.name,
                    coachName = people.find(trxClass.coachId)?.name.orEmpty(),
                    visibility = trxClass.visibility,
                    slotsByWeekday = trxClass.slotsByWeekday,
                )
            }
        return InvitesUiState(
            invites = invites,
            pendingInviteCount = invites.size,
            isEmpty = invites.isEmpty(),
            confirmation = confirmation,
            error = error,
            connection = connection,
            navigation = navigation,
        )
    }
}
