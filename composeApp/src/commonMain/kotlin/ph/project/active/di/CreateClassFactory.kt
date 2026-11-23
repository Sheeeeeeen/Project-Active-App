package ph.project.active.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ph.project.active.data.model.SignedInAccount
import ph.project.active.presentation.createclass.CreateClassViewModel

@Composable
fun rememberCreateClass(
    account: SignedInAccount,
    factory: ((SignedInAccount) -> CreateClassViewModel)?,
): CreateClassViewModel {
    val key = "create-class:${account.id}"
    return if (factory == null) {
        koinViewModel(key = key) { parametersOf(account) }
    } else {
        viewModel(key = key) { factory(account) }
    }
}
