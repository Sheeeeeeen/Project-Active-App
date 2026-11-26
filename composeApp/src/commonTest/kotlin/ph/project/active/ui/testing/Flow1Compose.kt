package ph.project.active.ui.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import androidx.navigation.NavHostController
import ph.project.active.data.model.SignedInAccount
import ph.project.active.data.repository.AuthRepository
import ph.project.active.presentation.classdetail.ClassDetailViewModel
import ph.project.active.presentation.classdetail.JoinOutcome
import ph.project.active.presentation.coachattendance.CoachAttendanceViewModel
import ph.project.active.presentation.coachclasses.CoachClassesViewModel
import ph.project.active.presentation.createclass.CreateClassViewModel
import ph.project.active.presentation.dailyqr.DailyQrViewModel
import ph.project.active.presentation.manageclass.ManageClassViewModel
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.presentation.invites.InvitesViewModel
import ph.project.active.presentation.registration.RegistrationUiState
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.presentation.root.RootViewModel
import ph.project.active.presentation.studentclasses.StudentClassesViewModel
import ph.project.active.ui.TrxApp
import ph.project.active.ui.navigation.AppNavHost
import ph.project.active.ui.navigation.ClassDetail
import ph.project.active.ui.navigation.CoachHome
import ph.project.active.ui.navigation.GuestGraph
import ph.project.active.ui.navigation.GuestHome
import ph.project.active.ui.navigation.Registration
import ph.project.active.ui.navigation.StudentHome
import ph.project.active.ui.platform.NoShakeSource
import ph.project.active.ui.platform.ScreenBrightness
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
        classDetailViewModel: (String, SignedInAccount?, JoinOutcome?) -> ClassDetailViewModel,
        studentClassesViewModel: (SignedInAccount) -> StudentClassesViewModel,
        invitesViewModel: (SignedInAccount) -> InvitesViewModel,
        coachClassesViewModel: (SignedInAccount) -> CoachClassesViewModel,
        createClassViewModel: (SignedInAccount) -> CreateClassViewModel,
        manageClassViewModel: (SignedInAccount, String) -> ManageClassViewModel,
        shakeSource: ShakeSource = NoShakeSource,
        navController: NavHostController? = null,
        dailyQrViewModel: ((SignedInAccount) -> DailyQrViewModel)? = null,
        screenBrightness: ScreenBrightness = remember { FakeScreenBrightness() },
        coachAttendanceViewModel: ((SignedInAccount) -> CoachAttendanceViewModel)? = null,
        onNavController: (NavHostController) -> Unit,
    ) {
        if (navController == null) {
            AppNavHost(
                rootViewModel = rootViewModel,
                guestClassListViewModel = guestClassListViewModel,
                registrationViewModel = registrationViewModel,
                shakeSource = shakeSource,
                onNavController = onNavController,
                classDetailViewModel = classDetailViewModel,
                studentClassesViewModel = studentClassesViewModel,
                invitesViewModel = invitesViewModel,
                coachClassesViewModel = coachClassesViewModel,
                createClassViewModel = createClassViewModel,
                manageClassViewModel = manageClassViewModel,
                dailyQrViewModel = dailyQrViewModel,
                screenBrightness = screenBrightness,
                coachAttendanceViewModel = coachAttendanceViewModel,
            )
        } else {
            AppNavHost(
                rootViewModel = rootViewModel,
                guestClassListViewModel = guestClassListViewModel,
                registrationViewModel = registrationViewModel,
                shakeSource = shakeSource,
                onNavController = onNavController,
                classDetailViewModel = classDetailViewModel,
                studentClassesViewModel = studentClassesViewModel,
                invitesViewModel = invitesViewModel,
                coachClassesViewModel = coachClassesViewModel,
                createClassViewModel = createClassViewModel,
                manageClassViewModel = manageClassViewModel,
                navController = navController,
                dailyQrViewModel = dailyQrViewModel,
                screenBrightness = screenBrightness,
                coachAttendanceViewModel = coachAttendanceViewModel,
            )
        }
    }

    @Composable
    fun App(
        rootViewModel: RootViewModel,
        guestClassListViewModel: GuestClassListViewModel,
        registrationViewModel: (RegistrationOrigin) -> RegistrationViewModel,
        classDetailViewModel: (String, SignedInAccount?, JoinOutcome?) -> ClassDetailViewModel,
        studentClassesViewModel: (SignedInAccount) -> StudentClassesViewModel,
        invitesViewModel: (SignedInAccount) -> InvitesViewModel,
        coachClassesViewModel: (SignedInAccount) -> CoachClassesViewModel,
        createClassViewModel: (SignedInAccount) -> CreateClassViewModel,
        manageClassViewModel: (SignedInAccount, String) -> ManageClassViewModel,
        shakeSource: ShakeSource = NoShakeSource,
    ) {
        TrxApp(
            rootViewModel = rootViewModel,
            guestClassListViewModel = guestClassListViewModel,
            registrationViewModel = registrationViewModel,
            shakeSource = shakeSource,
            classDetailViewModel = classDetailViewModel,
            studentClassesViewModel = studentClassesViewModel,
            invitesViewModel = invitesViewModel,
            coachClassesViewModel = coachClassesViewModel,
            createClassViewModel = createClassViewModel,
            manageClassViewModel = manageClassViewModel,
        )
    }

    fun screenRoutes(nav: NavHostController): List<String> = nav.currentBackStack.value.mapNotNull { entry ->
        val destination = entry.destination
        when {
            destination.hasRoute(GuestHome::class) -> "GuestHome"
            destination.hasRoute(Registration::class) -> "Registration(${entry.toRoute<Registration>().requestClassId})"
            destination.hasRoute(StudentHome::class) -> "StudentHome(${entry.toRoute<StudentHome>().openClassId})"
            destination.hasRoute(ClassDetail::class) -> "ClassDetail(${entry.toRoute<ClassDetail>().classId})"
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

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
fun runFlow1UiTest(block: ComposeUiTest.() -> Unit) {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    try {
        runComposeUiTest(block = block)
    } finally {
        Dispatchers.resetMain()
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
            classDetailViewModel = world.detailFactory(),
            studentClassesViewModel = world.studentClassesFactory(),
            invitesViewModel = world.invitesFactory(),
            coachClassesViewModel = world.coachClassesFactory(),
            createClassViewModel = world.createClassFactory(),
            manageClassViewModel = world.manageClassFactory(),
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
            classDetailViewModel = world.detailFactory(),
            studentClassesViewModel = world.studentClassesFactory(),
            invitesViewModel = world.invitesFactory(),
            coachClassesViewModel = world.coachClassesFactory(),
            createClassViewModel = world.createClassFactory(),
            manageClassViewModel = world.manageClassFactory(),
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
