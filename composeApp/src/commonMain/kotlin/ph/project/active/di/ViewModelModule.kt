package ph.project.active.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ph.project.active.data.model.SignedInAccount
import ph.project.active.presentation.classdetail.ClassDetailViewModel
import ph.project.active.presentation.classdetail.JoinOutcome
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.presentation.invites.InvitesViewModel
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.presentation.root.RootViewModel
import ph.project.active.presentation.studentclasses.StudentClassesViewModel

val ViewModelModule = module {
    viewModel { RootViewModel(session = get(), network = get(), outbox = get()) }
    viewModel { GuestClassListViewModel(classes = get(), people = get(), network = get()) }
    viewModel { (classId: String, viewer: SignedInAccount?, joinOutcome: JoinOutcome?) ->
        ClassDetailViewModel(
            classId = classId,
            viewer = viewer,
            classes = get(),
            memberships = get(),
            people = get(),
            requestToJoin = get(),
            network = get(),
            joinOutcome = joinOutcome,
        )
    }
    viewModel { (account: SignedInAccount) ->
        StudentClassesViewModel(
            account = account,
            classes = get(),
            memberships = get(),
            people = get(),
            network = get(),
        )
    }
    viewModel { (account: SignedInAccount) ->
        InvitesViewModel(
            account = account,
            classes = get(),
            memberships = get(),
            people = get(),
            acceptInvite = get(),
            declineInvite = get(),
            network = get(),
        )
    }
    viewModel { (origin: RegistrationOrigin) ->
        RegistrationViewModel(
            origin = origin,
            auth = get(),
            session = get(),
            people = get(),
            classes = get(),
            requestToJoin = get(),
            network = get(),
        )
    }
}
