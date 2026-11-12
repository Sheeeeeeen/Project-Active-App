package ph.project.active.ui.registration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.ui.collectNow
import ph.project.active.ui.navigation.NavigationEffect
import ph.project.active.ui.platform.ShakeSource

@Composable
fun RegistrationRoute(
    origin: RegistrationOrigin,
    onBack: () -> Unit,
    viewModel: RegistrationViewModel = koinViewModel { parametersOf(origin) },
    shakeSource: ShakeSource = koinInject(),
    onNavigate: (Navigation) -> Unit = {},
    shakeTicks: MutableIntState? = null,
) {
    val state = viewModel.state.collectNow()
    val ticks = shakeTicks ?: remember { mutableIntStateOf(0) }
    if (shakeTicks == null) {
        LaunchedEffect(shakeSource) {
            shakeSource.shakes.collect { ticks.intValue = ticks.intValue + 1 }
        }
    }
    val applied = remember { mutableIntStateOf(0) }
    LaunchedEffect(ticks.intValue) {
        while (applied.intValue < ticks.intValue) {
            viewModel.onShake()
            applied.intValue = applied.intValue + 1
        }
    }
    NavigationEffect(
        navigation = state.navigation,
        onNavigationHandled = viewModel::onNavigationHandled,
        onNavigate = onNavigate,
    )
    fun flushNavigation() {
        val event = viewModel.state.value.navigation ?: return
        onNavigate(event)
        viewModel.onNavigationHandled()
    }
    RegistrationScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onEmployeeIdChange = viewModel::onEmployeeIdChange,
        onSignInEmailChange = viewModel::onSignInEmailChange,
        onSignInPasswordChange = viewModel::onSignInPasswordChange,
        onSubmit = {
            viewModel.onSubmit()
            flushNavigation()
        },
        onSignIn = {
            viewModel.onSignIn()
            flushNavigation()
        },
        onBack = onBack,
    )
}
