package ph.project.active.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import ph.project.active.domain.model.Role
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.presentation.root.Root
import ph.project.active.presentation.root.RootViewModel
import ph.project.active.ui.collectNow
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
    SideEffect { onNavController(navController) }
    val startDestination = remember {
        when (rootViewModel.state.value.root) {
            Root.Guest -> GuestGraph
            is Root.Student -> StudentHome(openClassId = null)
            is Root.Coach -> CoachHome
        }
    }
    val rootState = rootViewModel.state.collectNow()
    val guestState = guestClassListViewModel.state.collectNow()
    val shakeTicks = remember { mutableIntStateOf(0) }
    DisposableEffect(shakeSource) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        scope.launch {
            shakeSource.shakes.collect { shakeTicks.intValue = shakeTicks.intValue + 1 }
        }
        onDispose { scope.cancel() }
    }
    NavHost(navController = navController, startDestination = startDestination) {
        navigation<GuestGraph>(startDestination = GuestHome) {
            composable<GuestHome> {
                NavigationEffect(
                    navigation = guestState.navigation,
                    onNavigationHandled = guestClassListViewModel::onNavigationHandled,
                    onNavigate = { event -> navController.follow(event) },
                )
                GuestHomePlaceholder(onRegisterOrSignIn = guestClassListViewModel::onRegisterOrSignIn)
            }
            composable<Registration> { entry ->
                val requestClassId = entry.toRoute<Registration>().requestClassId
                val origin = requestClassId?.let { RegistrationOrigin.FromRequestToJoin(it) }
                    ?: RegistrationOrigin.FromClassList
                val provided = registrationViewModel
                if (provided != null) {
                    RegistrationRoute(
                        origin = origin,
                        viewModel = provided(origin),
                        shakeSource = shakeSource,
                        onBack = { navController.popBackStack() },
                        onNavigate = { event -> navController.follow(event) },
                        shakeTicks = shakeTicks,
                    )
                } else {
                    RegistrationRoute(
                        origin = origin,
                        shakeSource = shakeSource,
                        onBack = { navController.popBackStack() },
                        onNavigate = { event -> navController.follow(event) },
                        shakeTicks = shakeTicks,
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

private fun NavHostController.follow(event: Navigation) {
    when (event) {
        is Navigation.OpenRegistration -> {
            val classId = (event.origin as? RegistrationOrigin.FromRequestToJoin)?.classId
            navigate(Registration(classId)) { launchSingleTop = true }
        }
        is Navigation.OpenSignedInRoot -> when (event.role) {
            Role.COACH -> navigate(CoachHome) {
                popUpTo<GuestGraph> { inclusive = true }
                launchSingleTop = true
            }
            Role.STUDENT -> navigate(StudentHome(event.openClassId)) {
                popUpTo<GuestGraph> { inclusive = true }
                launchSingleTop = true
            }
        }
        else -> Unit
    }
}
