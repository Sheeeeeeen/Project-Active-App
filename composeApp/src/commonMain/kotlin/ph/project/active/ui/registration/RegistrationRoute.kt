package ph.project.active.ui.registration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.ui.navigation.LocalRegistrationShakes
import ph.project.active.ui.navigation.RegistrationShakeCollection
import ph.project.active.ui.platform.ShakeSource

@Composable
fun RegistrationRoute(
    origin: RegistrationOrigin,
    onBack: () -> Unit,
    viewModel: RegistrationViewModel = koinViewModel { parametersOf(origin) },
    shakeSource: ShakeSource = koinInject(),
    onNavigate: (Navigation) -> Unit = {},
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val scope = rememberCoroutineScope()
    val parentShakes = LocalRegistrationShakes.current
    val shakes = parentShakes ?: remember { RegistrationShakeCollection() }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(shakes, viewModel, shakeSource, lifecycleOwner, origin) {
        val collectShakes = origin !is RegistrationOrigin.FromRequestToJoin
        val observer = LifecycleEventObserver { _, event ->
            if (!collectShakes) return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_RESUME -> shakes.ensure(scope, shakeSource, viewModel)
                Lifecycle.Event.ON_PAUSE -> shakes.stop(viewModel)
                else -> Unit
            }
        }
        if (collectShakes) shakes.ensure(scope, shakeSource, viewModel)
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (collectShakes) shakes.stop(viewModel)
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.state.collect { current ->
            val event = current.navigation ?: return@collect
            viewModel.onNavigationHandled()
            onNavigate(event)
        }
    }
    RegistrationScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onEmployeeIdChange = viewModel::onEmployeeIdChange,
        onSignInEmailChange = viewModel::onSignInEmailChange,
        onSignInPasswordChange = viewModel::onSignInPasswordChange,
        onSubmit = viewModel::onSubmit,
        onSignIn = viewModel::onSignIn,
        onBack = onBack,
    )
}
