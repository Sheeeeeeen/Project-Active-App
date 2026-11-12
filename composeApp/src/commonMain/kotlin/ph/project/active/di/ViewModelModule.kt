package ph.project.active.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.presentation.root.RootViewModel

val ViewModelModule = module {
    viewModel { RootViewModel(session = get(), network = get(), outbox = get()) }
    viewModel { GuestClassListViewModel(classes = get(), people = get(), network = get()) }
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
