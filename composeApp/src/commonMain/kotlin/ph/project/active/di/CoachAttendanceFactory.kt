package ph.project.active.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ph.project.active.data.model.SignedInAccount
import ph.project.active.presentation.coachattendance.CoachAttendanceViewModel

@Composable
fun rememberCoachAttendance(
    account: SignedInAccount,
    factory: ((SignedInAccount) -> CoachAttendanceViewModel)?,
): CoachAttendanceViewModel {
    val key = "coach-attendance:${account.id}"
    return if (factory == null) {
        koinViewModel(key = key) { parametersOf(account) }
    } else {
        viewModel(key = key) { factory(account) }
    }
}
