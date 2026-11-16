package ph.project.active.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf
import ph.project.active.data.model.SignedInAccount
import ph.project.active.domain.repository.inmemory.InMemoryMembershipRepository
import ph.project.active.domain.usecase.RequestToJoin
import ph.project.active.presentation.classdetail.ClassDetailViewModel
import ph.project.active.presentation.classdetail.JoinOutcome
import ph.project.active.presentation.common.DetailGraph

@Composable
fun rememberClassDetail(
    classId: String,
    viewer: SignedInAccount?,
    joinOutcome: JoinOutcome?,
): ClassDetailViewModel {
    if (GlobalContext.getOrNull() != null) {
        return koinViewModel { parametersOf(classId, viewer, joinOutcome) }
    }
    return viewModel(key = "$classId:${viewer?.id}:${joinOutcome?.name}") {
        val classes = checkNotNull(DetailGraph.classes)
        val people = checkNotNull(DetailGraph.people)
        val network = checkNotNull(DetailGraph.network)
        val memberships = DetailGraph.memberships ?: InMemoryMembershipRepository()
        ClassDetailViewModel(
            classId = classId,
            viewer = viewer,
            classes = classes,
            memberships = memberships,
            people = people,
            requestToJoin = DetailGraph.requestToJoin ?: RequestToJoin(classes, memberships),
            network = network,
            joinOutcome = joinOutcome,
        )
    }
}
