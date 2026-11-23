package ph.project.active.ui.createclass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ph.project.active.data.model.SignedInAccount
import ph.project.active.di.rememberCreateClass
import ph.project.active.presentation.createclass.CreateClassViewModel

@Composable
fun CreateClassRoute(
    account: SignedInAccount,
    factory: ((SignedInAccount) -> CreateClassViewModel)?,
    onClose: () -> Unit,
) {
    val viewModel = rememberCreateClass(account, factory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    CreateClassScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onSpecificDays = viewModel::onSpecificDays,
        onEveryDay = viewModel::onEveryDay,
        onToggleDay = viewModel::onToggleDay,
        onAddSlot = viewModel::onAddSlot,
        onRemoveSlot = viewModel::onRemoveSlot,
        onVisibilityChange = viewModel::onVisibilityChange,
        onEndDateChange = viewModel::onEndDateChange,
        onSave = viewModel::onSave,
        onCancel = {
            viewModel.onCancel()
            onClose()
        },
        onSaved = onClose,
    )
}
