package ph.project.active.presentation.manageclass

sealed interface InviteConfirmation {
    data class InviteAccepted(val className: String) : InviteConfirmation
}
