package ph.project.active.di

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.test.KoinTest
import org.koin.test.get
import ph.project.active.data.fake.FakeAuthRepository
import ph.project.active.data.repository.AuthRepository
import ph.project.active.data.repository.SessionStore
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.presentation.root.RootViewModel

class F1Ac7KoinGraphTest : KoinTest {
    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun F1_AC7_appModulesResolveFlow1ViewModelsAndASharedSession() {
        startKoin { modules(appModules()) }
        get<RootViewModel>()
        get<GuestClassListViewModel>()
        get<RegistrationViewModel> { parametersOf(RegistrationOrigin.FromClassList) }
        get<RegistrationViewModel> { parametersOf(RegistrationOrigin.FromRequestToJoin("c1")) }
        assertTrue(get<AuthRepository>() is FakeAuthRepository)
        assertSame(get<SessionStore>(), get<SessionStore>())
    }
}
