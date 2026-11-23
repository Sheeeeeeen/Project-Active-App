package ph.project.active.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ph.project.active.data.model.SignedInAccount
import ph.project.active.presentation.manageclass.ManageClassViewModel

@Composable
fun rememberManageClass(
    account: SignedInAccount,
    classId: String,
    factory: ((SignedInAccount, String) -> ManageClassViewModel)?,
): ManageClassViewModel {
    val key = "manage-class:${account.id}:$classId"
    return if (factory == null) {
        koinViewModel(key = key) { parametersOf(account, classId) }
    } else {
        viewModel(key = key) { factory(account, classId) }
    }
}
