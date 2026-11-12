package ph.project.active.ui.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import kotlin.test.assertTrue
import androidx.navigation.NavHostController
import ph.project.active.data.repository.AuthRepository
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.presentation.registration.RegistrationUiState
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.presentation.root.RootViewModel
import ph.project.active.ui.TrxApp
import ph.project.active.ui.navigation.AppNavHost
import ph.project.active.ui.navigation.CoachHome
import ph.project.active.ui.navigation.GuestGraph
import ph.project.active.ui.navigation.GuestHome
import ph.project.active.ui.navigation.Registration
import ph.project.active.ui.navigation.StudentHome
import ph.project.active.ui.platform.NoShakeSource
import ph.project.active.ui.platform.ShakeSource
import ph.project.active.ui.registration.RegistrationRoute
import ph.project.active.ui.registration.RegistrationScreen
import androidx.navigation.toRoute

object Flow1Compose {
    @Composable
    fun Screen(
        state: RegistrationUiState,
        onNameChange: (String) -> Unit = {},
        onEmailChange: (String) -> Unit = {},
        onPasswordChange: (String) -> Unit = {},
        onEmployeeIdChange: (String) -> Unit = {},
        onSignInEmailChange: (String) -> Unit = {},
        onSignInPasswordChange: (String) -> Unit = {},
        onSubmit: () -> Unit = {},
        onSignIn: () -> Unit = {},
        onBack: () -> Unit = {},
    ) {
        RegistrationScreen(
            state = state,
            onNameChange = onNameChange,
            onEmailChange = onEmailChange,
            onPasswordChange = onPasswordChange,
            onEmployeeIdChange = onEmployeeIdChange,
            onSignInEmailChange = onSignInEmailChange,
            onSignInPasswordChange = onSignInPasswordChange,
            onSubmit = onSubmit,
            onSignIn = onSignIn,
            onBack = onBack,
        )
    }

    @Composable
    fun Bound(viewModel: RegistrationViewModel, onBack: () -> Unit = {}) {
        val state by viewModel.state.collectAsState()
        Screen(
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

    @Composable
    fun Route(
        viewModel: RegistrationViewModel,
        shakeSource: ShakeSource,
        onBack: () -> Unit = {},
    ) {
        RegistrationRoute(
            origin = viewModel.state.value.origin,
            viewModel = viewModel,
            shakeSource = shakeSource,
            onBack = onBack,
        )
    }

    @Composable
    fun Nav(
        rootViewModel: RootViewModel,
        guestClassListViewModel: GuestClassListViewModel,
        registrationViewModel: (RegistrationOrigin) -> RegistrationViewModel,
        shakeSource: ShakeSource = NoShakeSource,
        onNavController: (NavHostController) -> Unit,
    ) {
        AppNavHost(
            rootViewModel = rootViewModel,
            guestClassListViewModel = guestClassListViewModel,
            registrationViewModel = registrationViewModel,
            shakeSource = shakeSource,
            onNavController = onNavController,
        )
    }

    @Composable
    fun App(
        rootViewModel: RootViewModel,
        guestClassListViewModel: GuestClassListViewModel,
        registrationViewModel: (RegistrationOrigin) -> RegistrationViewModel,
        shakeSource: ShakeSource = NoShakeSource,
    ) {
        TrxApp(
            rootViewModel = rootViewModel,
            guestClassListViewModel = guestClassListViewModel,
            registrationViewModel = registrationViewModel,
            shakeSource = shakeSource,
        )
    }

    fun screenRoutes(nav: NavHostController): List<String> = nav.currentBackStack.value.mapNotNull { entry ->
        val destination = entry.destination
        when {
            destination.hasRoute(GuestHome::class) -> "GuestHome"
            destination.hasRoute(Registration::class) -> "Registration(${entry.toRoute<Registration>().requestClassId})"
            destination.hasRoute(StudentHome::class) -> "StudentHome(${entry.toRoute<StudentHome>().openClassId})"
            destination.hasRoute(CoachHome::class) -> "CoachHome"
            else -> null
        }
    }

    fun guestHomeIsInsideGuestGraph(nav: NavHostController): Boolean {
        val guest = nav.currentBackStack.value.firstOrNull { it.destination.hasRoute(GuestHome::class) } ?: return false
        return guest.destination.parent?.hasRoute(GuestGraph::class) == true
    }

    fun hasGuestRoute(nav: NavHostController): Boolean = nav.currentBackStack.value.any { entry ->
        val destination = entry.destination
        destination.hasRoute(GuestGraph::class) ||
            destination.hasRoute(GuestHome::class) ||
            destination.hasRoute(Registration::class) ||
            destination.parent?.hasRoute(GuestGraph::class) == true
    }
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.showBound(viewModel: RegistrationViewModel, onBack: () -> Unit = {}) {
    setContent { Flow1Compose.Bound(viewModel, onBack) }
    waitForIdle()
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.showScreen(state: RegistrationUiState) {
    setContent { Flow1Compose.Screen(state) }
    waitForIdle()
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.showRoute(
    viewModel: RegistrationViewModel,
    shakeSource: ShakeSource,
    onBack: () -> Unit = {},
) {
    setContent { Flow1Compose.Route(viewModel, shakeSource, onBack) }
    waitForIdle()
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.mountNav(
    world: Flow1Harness,
    auth: AuthRepository = world.auth,
    shakeSource: ShakeSource = NoShakeSource,
    factory: ((RegistrationOrigin) -> RegistrationViewModel)? = null,
): NavHostController {
    lateinit var nav: NavHostController
    val root = world.root()
    val guest = world.guest()
    val registrationViewModel = factory ?: world.factory(auth)
    setContent {
        Flow1Compose.Nav(
            rootViewModel = root,
            guestClassListViewModel = guest,
            registrationViewModel = registrationViewModel,
            shakeSource = shakeSource,
            onNavController = { nav = it },
        )
    }
    waitForIdle()
    return nav
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.showApp(world: Flow1Harness, shakeSource: ShakeSource = NoShakeSource) {
    val root = world.root()
    val guest = world.guest()
    val registrationViewModel = world.factory()
    setContent {
        Flow1Compose.App(
            rootViewModel = root,
            guestClassListViewModel = guest,
            registrationViewModel = registrationViewModel,
            shakeSource = shakeSource,
        )
    }
    waitForIdle()
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.openRegistration(nav: NavHostController, classId: String?) {
    runOnIdle {
        nav.navigate(Registration(classId)) { launchSingleTop = true }
    }
    waitForIdle()
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.tag(name: String): SemanticsNodeInteraction = onNodeWithTag(name, useUnmergedTree = true)

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.mergedTag(name: String): SemanticsNodeInteraction = onNodeWithTag(name)

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.typeInto(name: String, value: String) {
    tag(name).performTextInput(value)
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.replaceText(name: String, value: String) {
    tag(name).performTextReplacement(value)
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.clickTag(name: String) {
    tag(name).performScrollTo().performClick()
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertShown(text: String) {
    val found = onAllNodesWithText(text, useUnmergedTree = true).fetchSemanticsNodes()
    assertTrue(found.isNotEmpty(), "missing copy: $text")
}
