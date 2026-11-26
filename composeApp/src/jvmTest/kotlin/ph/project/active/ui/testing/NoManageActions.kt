package ph.project.active.ui.testing

import ph.project.active.ui.manageclass.ManageClassActions

/** Manage actions that do nothing, for showing the stateless Manage screen with a hand-made state. */
val noManageActions = ManageClassActions(
    onSelectTab = {},
    onCancelDate = {},
    onKeepDate = {},
    onConfirmCancelDate = {},
    onApprove = {},
    onDecline = {},
    onRemove = {},
    onKeep = {},
    onConfirmRemove = {},
    onSearchTextChange = {},
    onSearch = {},
    onSendInvite = {},
    onOpenOptions = {},
    onCloseOptions = {},
    onCancelWholeClass = {},
    onKeepClass = {},
    onConfirmCancelClass = {},
    onConfirmationHandled = {},
    onNavigationHandled = {},
)
