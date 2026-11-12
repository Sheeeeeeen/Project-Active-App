package ph.project.active.ui

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.presentation.root.RootViewModel
import ph.project.active.ui.navigation.AppNavHost
import ph.project.active.ui.platform.ShakeSource
import ph.project.active.ui.theme.TrxTheme

@Composable
fun TrxApp(
    rootViewModel: RootViewModel = koinViewModel(),
    guestClassListViewModel: GuestClassListViewModel = koinViewModel(),
    registrationViewModel: ((RegistrationOrigin) -> RegistrationViewModel)? = null,
    shakeSource: ShakeSource = koinInject(),
) {
    TrxTheme {
        AppNavHost(
            rootViewModel = rootViewModel,
            guestClassListViewModel = guestClassListViewModel,
            registrationViewModel = registrationViewModel,
            shakeSource = shakeSource,
        )
    }
}
