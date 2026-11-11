package ph.project.active.presentation

sealed interface InviteConfirmation {
    data class InviteAccepted(val className: String) : InviteConfirmation
}
