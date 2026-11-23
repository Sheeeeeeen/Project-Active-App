package ph.project.active.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.LifecycleStartEffect
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
import kotlin.reflect.typeOf
import ph.project.active.data.model.SignedInAccount
import ph.project.active.di.rememberClassDetail
import ph.project.active.domain.model.Role
import ph.project.active.presentation.classdetail.ClassDetailViewModel
import ph.project.active.presentation.classdetail.JoinOutcome
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.presentation.invites.InvitesViewModel
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.presentation.root.Root
import ph.project.active.presentation.root.RootViewModel
import ph.project.active.presentation.studentclasses.StudentClassesViewModel
import ph.project.active.ui.classdetail.ClassDetailScreen
import ph.project.active.ui.guestclasses.GuestClassListScreen
import ph.project.active.ui.home.CoachHomePlaceholder
import ph.project.active.ui.studenthome.StudentHomeScreen
import ph.project.active.ui.platform.ShakeSource
import ph.project.active.ui.registration.RegistrationRoute

@Composable
fun AppNavHost(
    rootViewModel: RootViewModel = koinViewModel(),
    guestClassListViewModel: GuestClassListViewModel = koinViewModel(),
    registrationViewModel: ((RegistrationOrigin) -> RegistrationViewModel)? = null,
    shakeSource: ShakeSource = koinInject(),
    onNavController: (NavHostController) -> Unit = {},
    classDetailViewModel: ((classId: String, viewer: SignedInAccount?, joinOutcome: JoinOutcome?) -> ClassDetailViewModel)? = null,
    studentClassesViewModel: ((SignedInAccount) -> StudentClassesViewModel)? = null,
    invitesViewModel: ((SignedInAccount) -> InvitesViewModel)? = null,
    navController: NavHostController = rememberNavController(),
) {
    val scope = rememberCoroutineScope()
    val shakes = remember { RegistrationShakeCollection() }
    SideEffect { onNavController(navController) }
    val startDestination = remember {
        when (rootViewModel.startRoot) {
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
                    LifecycleStartEffect(guestClassListViewModel) {
                        guestClassListViewModel.refresh()
                        onStopOrDispose {}
                    }
                    val listState by guestClassListViewModel.state.collectAsStateWithLifecycle()
                    GuestClassListScreen(
                        state = listState,
                        onClassTapped = { id ->
                            guestClassListViewModel.onClassTapped(id)
                            guestClassListViewModel.state.value.navigation?.let { event ->
                                guestClassListViewModel.onNavigationHandled()
                                navController.follow(event, shakes, scope, shakeSource, registrationViewModel)
                            }
                        },
                        onRegisterOrSignIn = {
                            guestClassListViewModel.onRegisterOrSignIn()
                            guestClassListViewModel.state.value.navigation?.let { event ->
                                guestClassListViewModel.onNavigationHandled()
                                navController.follow(event, shakes, scope, shakeSource, registrationViewModel)
                            }
                        },
                    )
                }
                composable<Registration> { entry ->
                    val classId = entry.toRoute<Registration>().requestClassId
                    val origin = if (classId == null) {
                        RegistrationOrigin.FromClassList
                    } else {
                        RegistrationOrigin.FromRequestToJoin(classId)
                    }
                    val onNavigate: (Navigation) -> Unit = { event ->
                        navController.follow(event, shakes, scope, shakeSource, registrationViewModel)
                    }
                    val factory = registrationViewModel
                    if (factory == null) {
                        RegistrationRoute(
                            origin = origin,
                            onBack = { navController.popBackStack() },
                            shakeSource = shakeSource,
                            onNavigate = onNavigate,
                        )
                    } else {
                        RegistrationRoute(
                            origin = origin,
                            onBack = { navController.popBackStack() },
                            viewModel = remember(origin) { factory(origin) },
                            shakeSource = shakeSource,
                            onNavigate = onNavigate,
                        )
                    }
                }
            }
            composable<ClassDetail>(typeMap = joinOutcomeType) { entry ->
                val route = entry.toRoute<ClassDetail>()
                val viewer = when (val root = rootState.root) {
                    is Root.Student -> root.account
                    is Root.Coach -> root.account
                    Root.Guest -> null
                }
                val detailViewModel = rememberClassDetail(
                    classId = route.classId,
                    viewer = viewer,
                    joinOutcome = route.joinOutcome,
                    factory = classDetailViewModel,
                )
                val detailState by detailViewModel.state.collectAsStateWithLifecycle()
                ClassDetailScreen(
                    state = detailState,
                    onRequestToJoin = {
                        detailViewModel.onRequestToJoin()
                        detailViewModel.state.value.navigation?.let { event ->
                            detailViewModel.onNavigationHandled()
                            navController.follow(event, shakes, scope, shakeSource, registrationViewModel)
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable<StudentHome>(typeMap = joinOutcomeType) {
                val account = (rootState.root as? Root.Student)?.account
                if (account != null) {
                    StudentHomeScreen(
                        account = account,
                        studentClassesViewModel = studentClassesViewModel,
                        invitesViewModel = invitesViewModel,
                        onOpenClass = { id ->
                            navController.follow(Navigation.OpenClassDetail(id), shakes, scope, shakeSource, registrationViewModel)
                        },
                    )
                }
            }
            composable<CoachHome> {
                val name = (rootState.root as? Root.Coach)?.account?.name.orEmpty()
                CoachHomePlaceholder(name = name)
            }
        }
    }
}

private val joinOutcomeType = mapOf(typeOf<JoinOutcome?>() to JoinOutcomeNavType)

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
            if (event.origin is RegistrationOrigin.FromClassList) {
                registrationViewModel?.invoke(event.origin)?.let { viewModel ->
                    shakes.ensure(scope, shakeSource, viewModel)
                }
            }
        }
        is Navigation.OpenClassDetail -> {
            navigate(ClassDetail(event.classId)) { launchSingleTop = true }
        }
        is Navigation.OpenSignedInRoot -> {
            shakes.stopAny()
            val home = when (event.role) {
                Role.COACH -> CoachHome
                Role.STUDENT -> StudentHome(event.openClassId, event.joinOutcome)
            }
            // Drop the guest graph, including a class detail pushed above it.
            navigate(home) {
                popUpTo(graph.id) { inclusive = true }
                launchSingleTop = true
            }
            val classId = event.openClassId
            if (event.role == Role.STUDENT && classId != null) {
                navigate(ClassDetail(classId, event.joinOutcome)) { launchSingleTop = true }
            }
        }
        else -> Unit
    }
}
