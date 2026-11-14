package ph.project.active.presentation.invites

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.presentation.manageclass.InviteConfirmation
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.data.repository.PersonRepository
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.data.model.SignedInAccount
import ph.project.active.presentation.common.watchConnection
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.presentation.common.requireStudent
import ph.project.active.domain.usecase.DeclineInvite
import ph.project.active.domain.repository.currentStatus
import ph.project.active.domain.usecase.AcceptInvite
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.toConnection
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.asUiError
import ph.project.active.presentation.common.isOffline
import ph.project.active.presentation.common.UiError

class InvitesViewModel(
    account: SignedInAccount,
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    private val people: PersonRepository,
    private val acceptInvite: AcceptInvite,
    private val declineInvite: DeclineInvite,
    private val network: NetworkMonitor,
) : ViewModel() {
    init {
        requireStudent(account)
    }

    private val studentId = account.id
    private val _state = MutableStateFlow(
        load(confirmation = null, error = null, navigation = null, connection = network.status.value.toConnection()),
    )
    val state: StateFlow<InvitesUiState> = _state.asStateFlow()

    init {
        watchConnection(network, _state) { copy(connection = it) }
    }

    fun onAccept(classId: String) {
        if (network.isOffline()) {
            _state.update { it.copy(error = UiError.Offline) }
            return
        }
        try {
            acceptInvite(classId = classId, studentId = studentId)
            val name = classes.findClass(classId)?.name.orEmpty()
            _state.value = load(
                confirmation = InviteConfirmation.InviteAccepted(name),
                error = null,
                navigation = _state.value.navigation,
                connection = _state.value.connection,
            )
        } catch (e: IllegalStateException) {
            showFailure(e)
        } catch (e: IllegalArgumentException) {
            showFailure(e)
        }
    }

    fun onDecline(classId: String) {
        if (network.isOffline()) {
            _state.update { it.copy(error = UiError.Offline) }
            return
        }
        try {
            declineInvite(classId = classId, studentId = studentId)
            _state.value = load(
                confirmation = null,
                error = null,
                navigation = _state.value.navigation,
                connection = _state.value.connection,
            )
        } catch (e: IllegalStateException) {
            showFailure(e)
        } catch (e: IllegalArgumentException) {
            showFailure(e)
        }
    }

    fun onNavigationHandled() {
        _state.update { it.copy(navigation = null) }
    }

    private fun showFailure(error: Throwable) {
        _state.value = load(
            confirmation = _state.value.confirmation,
            error = error.asUiError(),
            navigation = _state.value.navigation,
            connection = _state.value.connection,
        )
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
