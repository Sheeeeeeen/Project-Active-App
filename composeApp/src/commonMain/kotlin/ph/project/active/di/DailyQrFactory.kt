package ph.project.active.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ph.project.active.data.model.SignedInAccount
import ph.project.active.presentation.dailyqr.DailyQrViewModel

@Composable
fun rememberDailyQr(
    account: SignedInAccount,
    factory: ((SignedInAccount) -> DailyQrViewModel)?,
): DailyQrViewModel {
    val key = "daily-qr:${account.id}"
    return if (factory == null) {
        koinViewModel(key = key) { parametersOf(account) }
    } else {
        viewModel(key = key) { factory(account) }
    }
}
