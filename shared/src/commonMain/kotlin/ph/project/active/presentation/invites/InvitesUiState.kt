package ph.project.active.presentation.invites

import ph.project.active.presentation.manageclass.InviteConfirmation
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.UiError

data class InvitesUiState(
    val invites: List<InviteRow>,
    val pendingInviteCount: Int,
    val isEmpty: Boolean,
    val confirmation: InviteConfirmation?,
    val error: UiError?,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
