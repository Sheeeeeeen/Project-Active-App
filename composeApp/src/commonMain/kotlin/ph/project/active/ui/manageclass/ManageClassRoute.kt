package ph.project.active.ui.manageclass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ph.project.active.data.model.SignedInAccount
import ph.project.active.di.rememberManageClass
import ph.project.active.presentation.manageclass.ManageClassViewModel

@Composable
fun ManageClassRoute(
    account: SignedInAccount,
    classId: String,
    factory: ((SignedInAccount, String) -> ManageClassViewModel)?,
    onBack: () -> Unit,
) {
    val viewModel = rememberManageClass(account, classId, factory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = remember(viewModel) {
        ManageClassActions(
            onSelectTab = viewModel::onSelectTab,
            onCancelDate = viewModel::onCancelDate,
            onKeepDate = viewModel::onKeepDate,
            onConfirmCancelDate = viewModel::onConfirmCancelDate,
            onApprove = viewModel::onApprove,
            onDecline = viewModel::onDecline,
            onRemove = viewModel::onRemove,
            onKeep = viewModel::onKeep,
            onConfirmRemove = viewModel::onConfirmRemove,
            onSearchTextChange = viewModel::onSearchTextChange,
            onSearch = viewModel::onSearch,
            onSendInvite = viewModel::onSendInvite,
            onOpenOptions = viewModel::onOpenOptions,
            onCloseOptions = viewModel::onCloseOptions,
            onCancelWholeClass = viewModel::onCancelWholeClass,
            onKeepClass = viewModel::onKeepClass,
            onConfirmCancelClass = viewModel::onConfirmCancelClass,
            onConfirmationHandled = viewModel::onConfirmationHandled,
            onNavigationHandled = viewModel::onNavigationHandled,
        )
    }
    ManageClassScreen(state = state, actions = actions, onBack = onBack)
}
