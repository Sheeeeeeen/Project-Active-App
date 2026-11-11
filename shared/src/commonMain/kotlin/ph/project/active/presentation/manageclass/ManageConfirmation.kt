package ph.project.active.presentation.manageclass

import kotlinx.datetime.LocalDate

sealed interface ManageConfirmation {
    data class DateCancelled(val date: LocalDate) : ManageConfirmation
    data class InviteSent(val name: String) : ManageConfirmation
}
