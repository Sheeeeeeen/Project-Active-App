package ph.project.active.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ph.project.active.data.model.SignedInAccount
import ph.project.active.presentation.studentsessions.StudentSessionsViewModel

@Composable
fun rememberStudentSessions(
    account: SignedInAccount,
    factory: ((SignedInAccount) -> StudentSessionsViewModel)?,
): StudentSessionsViewModel {
    val key = "student-sessions:${account.id}"
    return if (factory == null) {
        koinViewModel(key = key) { parametersOf(account) }
    } else {
        viewModel(key = key) { factory(account) }
    }
}
