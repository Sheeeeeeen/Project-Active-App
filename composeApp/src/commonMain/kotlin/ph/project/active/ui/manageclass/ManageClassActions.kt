package ph.project.active.ui.manageclass

import kotlinx.datetime.LocalDate
import ph.project.active.presentation.manageclass.ManageTab

/** Every Manage event, so the screen stays stateless and the route wires the ViewModel once. */
data class ManageClassActions(
    val onSelectTab: (ManageTab) -> Unit,
    val onCancelDate: (LocalDate) -> Unit,
    val onKeepDate: () -> Unit,
    val onConfirmCancelDate: () -> Unit,
    val onApprove: (String) -> Unit,
    val onDecline: (String) -> Unit,
    val onRemove: (String) -> Unit,
    val onKeep: () -> Unit,
    val onConfirmRemove: () -> Unit,
    val onSearchTextChange: (String) -> Unit,
    val onSearch: () -> Unit,
    val onSendInvite: () -> Unit,
    val onOpenOptions: () -> Unit,
    val onCloseOptions: () -> Unit,
    val onCancelWholeClass: () -> Unit,
    val onKeepClass: () -> Unit,
    val onConfirmCancelClass: () -> Unit,
    val onConfirmationHandled: () -> Unit,
    val onNavigationHandled: () -> Unit,
)
