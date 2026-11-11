package ph.project.active.presentation

data class InvitesUiState(
    val invites: List<InviteRow>,
    val pendingInviteCount: Int,
    val isEmpty: Boolean,
    val confirmation: InviteConfirmation?,
    val error: UiError?,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
