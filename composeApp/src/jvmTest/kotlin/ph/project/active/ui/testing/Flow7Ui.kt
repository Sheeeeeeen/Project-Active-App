package ph.project.active.ui.testing

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import ph.project.active.data.model.SignedInAccount
import ph.project.active.domain.crypto.Ed25519KeyPair
import ph.project.active.domain.model.AttendanceRecord
import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.UploadState
import ph.project.active.domain.model.User
import ph.project.active.domain.repository.inmemory.InMemoryCoachKeyRepository
import ph.project.active.domain.usecase.CancelDate
import ph.project.active.domain.usecase.GenerateDailyCode
import ph.project.active.ui.navigation.AppNavHost
import ph.project.active.ui.platform.CameraAccess
import ph.project.active.ui.platform.NoShakeSource
import ph.project.active.ui.theme.TrxTheme

/** Runs a flow 7 UI test in a window [width] wide (480 by default; F7-AC39 also uses 360 and 412) and 2400 tall. */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
fun runFlow7UiTest(width: Int = 480, block: suspend ComposeUiTest.() -> Unit) {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    try {
        runDesktopComposeUiTest(width = width, height = 2400) { block() }
    } finally {
        Dispatchers.resetMain()
    }
}

/**
 * Builds the flow 7 example (spec section 7): every class created on Mon 28 Sep 2026 at 5:00 AM, so each Mon 28
 * session is "Session 1"; Lunch Express cancelled on Mon 28 only; Paolo ACTIVE on all four; Paolo's Sunrise scan at
 * 6:02 AM (SYNCED). Ends on the default clock: Mon 28 Sep 2026, 5:31 PM, Asia/Manila, online.
 */
fun Flow4Harness.seedFlow7(): Flow7Example {
    setNow(MON_28_SEP_2026, 5, 0)
    val mara = mara()
    val leo = coach("Leo Ramirez")
    val paolo = student("Paolo Reyes", "EMP-20417")
    val sunrise = saveClass("Sunrise TRX Strength", listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), slot(6, 0, 6, 45))
    val lunchExpress = saveClass(
        "Lunch Express TRX",
        listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
        slot(12, 15, 12, 45),
    )
    val coreBurn = saveClass("TRX Core Burn", listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), slot(17, 30, 18, 30))
    val mobilityFlow = saveClass("TRX Mobility Flow", listOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY), slot(19, 0, 20, 0), coach = leo)
    CancelDate(classes, clock, timeZone).invoke(lunchExpress.id, MON_28_SEP_2026)
    for (trxClass in listOf(sunrise, lunchExpress, coreBurn, mobilityFlow)) joinByRequest(trxClass.id, paolo)
    paoloScan(sunrise, paolo, MON_28_SEP_2026, 6, 2, UploadState.SYNCED)
    setNow(MON_28_SEP_2026, 17, 31)
    val coachSide = InMemoryCoachKeyRepository()
    coachSide.save(mara.id, Ed25519KeyPair.generate())
    coachSide.save(leo.id, Ed25519KeyPair.generate())
    return Flow7Example(mara, leo, paolo, sunrise, lunchExpress, coreBurn, mobilityFlow, GenerateDailyCode(coachSide))
}

/** Saves a scan straight to the phone's store at [hour]:[minute] Asia/Manila on [date]. */
fun Flow4Harness.paoloScan(
    trxClass: TrxClass,
    student: User,
    date: LocalDate,
    hour: Int,
    minute: Int,
    uploadState: UploadState,
): AttendanceRecord {
    val record = AttendanceRecord(
        id = attendance.nextId(),
        sessionId = sessionIdOn(trxClass, date),
        classId = trxClass.id,
        studentId = student.id,
        scannedAt = LocalDateTime(date.year, date.month, date.day, hour, minute).toInstant(timeZone),
        uploadState = uploadState,
    )
    attendance.add(record)
    return record
}

/** Saves [user]'s session before anything is mounted, so the app starts on StudentHome. */
fun Flow4Harness.signInAs(user: User) {
    session.save(SignedInAccount(user.id, user.name, user.email, user.role, user.employeeId))
}

/**
 * Mounts the one NavHost in the light theme with the flow 4 world's factories plus the flow 7 Attend and Sessions
 * factories and the scanner, camera-permission and haptics fakes. No Koin. [lifecycle] stands in for background
 * and foreground. Call `signInAs(paolo)` first.
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.mountFlow7(
    world: Flow4Harness,
    scanner: FakeQrScanner = FakeQrScanner(),
    permission: FakeCameraPermission = FakeCameraPermission(CameraAccess.GRANTED),
    haptics: FakeScanHaptics = FakeScanHaptics(),
    lifecycle: HostLifecycle = HostLifecycle(),
): NavHostController {
    lateinit var nav: NavHostController
    setContent {
        CompositionLocalProvider(LocalLifecycleOwner provides lifecycle) {
            TrxTheme(dark = false) {
                AppNavHost(
                    rootViewModel = world.root(),
                    guestClassListViewModel = world.guest(),
                    registrationViewModel = world::registration,
                    shakeSource = NoShakeSource,
                    onNavController = { nav = it },
                    classDetailViewModel = world::detail,
                    studentClassesViewModel = world::studentClasses,
                    invitesViewModel = world::invites,
                    coachClassesViewModel = world::coachClasses,
                    createClassViewModel = world::createClass,
                    manageClassViewModel = world::manageClass,
                    dailyQrViewModel = world::dailyQr,
                    screenBrightness = world.brightness,
                    coachAttendanceViewModel = world::coachAttendance,
                    attendViewModel = world::attend,
                    studentSessionsViewModel = world::studentSessions,
                    qrScanner = scanner,
                    cameraPermission = permission,
                    scanHaptics = haptics,
                )
            }
        }
    }
    waitForIdle()
    return nav
}

/** Signs in as Paolo, mounts the app and opens the Attend tab (the class sheet opens by itself, F7-AC16). */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.openAttend(
    world: Flow4Harness,
    example: Flow7Example,
    scanner: FakeQrScanner = FakeQrScanner(),
    permission: FakeCameraPermission = FakeCameraPermission(CameraAccess.GRANTED),
    haptics: FakeScanHaptics = FakeScanHaptics(),
    lifecycle: HostLifecycle = HostLifecycle(),
): NavHostController {
    world.signInAs(example.paolo)
    val nav = mountFlow7(world, scanner, permission, haptics, lifecycle)
    openTab("attend")
    return nav
}

/** Signs in as Paolo, mounts the app and opens the Sessions tab. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.openSessions(world: Flow4Harness, example: Flow7Example): NavHostController {
    world.signInAs(example.paolo)
    val nav = mountFlow7(world)
    openTab("sessions")
    return nav
}

/** On the open sheet, taps the row for [trxClass] today and then Scan coach’s code. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.chooseAndScan(world: Flow4Harness, trxClass: TrxClass, date: LocalDate = MON_28_SEP_2026) {
    tap(choiceTag(world.sessionIdOn(trxClass, date)))
    tap("attend_sheet_scan")
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.tap(tagName: String) {
    tag(tagName).performClick()
    waitForIdle()
}

/** Hands [text] to the scanner screen as if the camera read it. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.scanText(scanner: FakeQrScanner, text: String) {
    runOnIdle { scanner.emit(text) }
    waitForIdle()
}

/** The system back gesture / button. On the desktop test host this is the Escape key on the focused window. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.pressSystemBack() {
    onRoot().performKeyInput { pressKey(Key.Escape) }
    waitForIdle()
}

fun choiceTag(sessionId: String): String = "attend_choice_$sessionId"

fun sessionsRowTag(sessionId: String): String = "sessions_row_$sessionId"

/** True when [expected] appears, in this order, among the texts under [tagName] (other texts may sit between). */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertTextsInOrder(tagName: String, expected: List<String>) {
    val actual = textsUnder(tagName)
    var at = 0
    for (text in actual) if (at < expected.size && text == expected[at]) at += 1
    assertEquals(expected.size, at, "expected $expected in this order under $tagName, found $actual")
}

/** No node with a tag ending in `_account` (the profile icon), anywhere on screen (decision 12). */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertNoProfileIcon() {
    val accounts = onAllNodes(hasTestTagSuffix("_account"), useUnmergedTree = true)
        .fetchSemanticsNodes(atLeastOneRootRequired = false)
    assertTrue(accounts.isEmpty(), "no profile icon, found ${accounts.map { it.config.getOrNull(SemanticsProperties.TestTag) }}")
}

/** No student tab is on screen (the scanner and result screens hide the tab bar). */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertNoTabBar() {
    for (name in listOf("classes", "attend", "sessions", "invites")) assertEquals(0, countTagged("student_tab_$name"), "the tab bar is hidden")
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertTabBarShown() {
    for (name in listOf("classes", "attend", "sessions", "invites")) assertEquals(1, countTagged("student_tab_$name"), "the tab bar is back")
}

/** Bounds of the node showing exactly [text] under [parentTag]. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.boundsOfTextUnder(parentTag: String, text: String): Rect {
    val nodes = onAllNodes(hasText(text) and hasAnyAncestor(hasTestTag(parentTag)), useUnmergedTree = true)
        .fetchSemanticsNodes(atLeastOneRootRequired = false)
    assertEquals(1, nodes.size, "one \"$text\" under $parentTag")
    return nodes.single().boundsInRoot
}

/** Bounds of the node tagged [tagName] under [parentTag]. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.boundsOfTagUnder(parentTag: String, tagName: String): Rect {
    val nodes = onAllNodes(hasTestTag(tagName) and hasAnyAncestor(hasTestTag(parentTag)), useUnmergedTree = true)
        .fetchSemanticsNodes(atLeastOneRootRequired = false)
    assertEquals(1, nodes.size, "one $tagName under $parentTag")
    return nodes.single().boundsInRoot
}

/** The value of the semantics property named [key] on the node tagged [tagName] (null when it is not set). */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.semanticsValue(tagName: String, key: String): Any? {
    val node: SemanticsNode = tag(tagName).fetchSemanticsNode()
    return node.config.firstOrNull { it.key.name == key }?.value
}

private fun hasTestTagSuffix(suffix: String): SemanticsMatcher =
    SemanticsMatcher("TestTag ends with $suffix") { node ->
        node.config.getOrNull(SemanticsProperties.TestTag)?.endsWith(suffix) == true
    }
