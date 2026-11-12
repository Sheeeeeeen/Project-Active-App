package ph.project.active.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ph.project.active.domain.model.Role
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.presentation.root.Root
import ph.project.active.presentation.root.RootViewModel
import ph.project.active.ui.home.CoachHomePlaceholder
import ph.project.active.ui.home.GuestHomePlaceholder
import ph.project.active.ui.home.StudentHomePlaceholder
import ph.project.active.ui.platform.ShakeSource
import ph.project.active.ui.registration.RegistrationRoute

@Composable
fun AppNavHost(
    rootViewModel: RootViewModel = koinViewModel(),
    guestClassListViewModel: GuestClassListViewModel = koinViewModel(),
    registrationViewModel: ((RegistrationOrigin) -> RegistrationViewModel)? = null,
    shakeSource: ShakeSource = koinInject(),
    onNavController: (NavHostController) -> Unit = {},
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val shakes = remember { RegistrationShakeCollection() }
    SideEffect { onNavController(navController) }
    val startDestination = remember {
        when (rootViewModel.state.value.root) {
            Root.Guest -> GuestGraph
            is Root.Student -> StudentHome(openClassId = null)
            is Root.Coach -> CoachHome
        }
    }
    val rootState by rootViewModel.state.collectAsStateWithLifecycle()
    CompositionLocalProvider(LocalRegistrationShakes provides shakes) {
        NavHost(navController = navController, startDestination = startDestination) {
            navigation<GuestGraph>(startDestination = GuestHome) {
                composable<GuestHome> {
                    LaunchedEffect(guestClassListViewModel) {
                        guestClassListViewModel.state.collect { state ->
                            val event = state.navigation ?: return@collect
                            guestClassListViewModel.onNavigationHandled()
                            navController.follow(
                                event = event,
                                shakes = shakes,
                                scope = scope,
                                shakeSource = shakeSource,
                                registrationViewModel = registrationViewModel,
                            )
                        }
                    }
                    GuestHomePlaceholder(onRegisterOrSignIn = guestClassListViewModel::onRegisterOrSignIn)
                }
                composable<Registration> { entry ->
                    val requestClassId = entry.toRoute<Registration>().requestClassId
                    val origin = requestClassId?.let { RegistrationOrigin.FromRequestToJoin(it) }
                        ?: RegistrationOrigin.FromClassList
                    val provided = registrationViewModel
                    val routeViewModel = provided?.let { factory ->
                        remember(origin) { factory(origin) }
                    }
                    if (routeViewModel != null) {
                        RegistrationRoute(
                            origin = origin,
                            viewModel = routeViewModel,
                            shakeSource = shakeSource,
                            onBack = { navController.popBackStack() },
                            onNavigate = { event ->
                                navController.follow(event, shakes, scope, shakeSource, registrationViewModel)
                            },
                        )
                    } else {
                        RegistrationRoute(
                            origin = origin,
                            shakeSource = shakeSource,
                            onBack = { navController.popBackStack() },
                            onNavigate = { event ->
                                navController.follow(event, shakes, scope, shakeSource, registrationViewModel)
                            },
                        )
                    }
                }
            }
            composable<StudentHome> { entry ->
                val openClassId = entry.toRoute<StudentHome>().openClassId
                val name = (rootState.root as? Root.Student)?.account?.name.orEmpty()
                StudentHomePlaceholder(name = name, openClassId = openClassId)
            }
            composable<CoachHome> {
                val name = (rootState.root as? Root.Coach)?.account?.name.orEmpty()
                CoachHomePlaceholder(name = name)
            }
        }
    }
}

private fun NavHostController.follow(
    event: Navigation,
    shakes: RegistrationShakeCollection,
    scope: CoroutineScope,
    shakeSource: ShakeSource,
    registrationViewModel: ((RegistrationOrigin) -> RegistrationViewModel)?,
) {
    when (event) {
        is Navigation.OpenRegistration -> {
            val classId = (event.origin as? RegistrationOrigin.FromRequestToJoin)?.classId
            navigate(Registration(classId)) { launchSingleTop = true }
            registrationViewModel?.invoke(event.origin)?.let { viewModel ->
                shakes.ensure(scope, shakeSource, viewModel)
            }
        }
        is Navigation.OpenSignedInRoot -> {
            shakes.stopAny()
            when (event.role) {
                Role.COACH -> navigate(CoachHome) {
                    popUpTo<GuestGraph> { inclusive = true }
                    launchSingleTop = true
                }
                Role.STUDENT -> navigate(StudentHome(event.openClassId)) {
                    popUpTo<GuestGraph> { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        else -> Unit
    }
}
