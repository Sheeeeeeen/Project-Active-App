package ph.project.active.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ph.project.active.data.model.SignedInAccount
import ph.project.active.presentation.classdetail.ClassDetailViewModel
import ph.project.active.presentation.classdetail.JoinOutcome

@Composable
fun rememberClassDetail(
    classId: String,
    viewer: SignedInAccount?,
    joinOutcome: JoinOutcome?,
    factory: ((classId: String, viewer: SignedInAccount?, joinOutcome: JoinOutcome?) -> ClassDetailViewModel)?,
): ClassDetailViewModel =
    if (factory == null) {
        koinViewModel { parametersOf(classId, viewer, joinOutcome) }
    } else {
        viewModel(key = "$classId:${viewer?.id}:${joinOutcome?.name}") { factory(classId, viewer, joinOutcome) }
    }
