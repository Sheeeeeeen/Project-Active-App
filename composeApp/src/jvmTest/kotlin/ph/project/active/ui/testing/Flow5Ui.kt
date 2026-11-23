package ph.project.active.ui.testing

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onRoot
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import ph.project.active.domain.usecase.CancelDate
import ph.project.active.ui.navigation.AppNavHost
import ph.project.active.ui.platform.NoShakeSource
import ph.project.active.ui.theme.TrxTheme

/** Mon 28 Sep 2026, the pinned "today" for flows 3 to 5. */
val MON_28_SEP_2026: LocalDate = LocalDate(2026, 9, 28)

/** Tue 29 Sep 2026, the next day for the code-rollover checks (F5-AC11). */
val TUE_29_SEP_2026: LocalDate = LocalDate(2026, 9, 29)

/** Runs a flow 5 UI test in the same tall window as flows 3 and 4. */
@OptIn(ExperimentalTestApi::class)
fun runFlow5UiTest(block: suspend ComposeUiTest.() -> Unit) = runFlow3UiTest(block)

/**
 * Mounts the one NavHost in the app's light theme, with the flow 4 world's factories plus the Daily QR
 * factory and the fake brightness. [lifecycle] stands in for the app going to the background and back;
 * setting [shown] to false disposes the whole app. Call `signInAsMara()` first to start on CoachHome.
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.mountFlow5(
    world: Flow4Harness,
    lifecycle: HostLifecycle = HostLifecycle(),
    shown: MutableState<Boolean> = mutableStateOf(true),
): NavHostController {
    lateinit var nav: NavHostController
    setContent {
        CompositionLocalProvider(LocalLifecycleOwner provides lifecycle) {
            TrxTheme(dark = false) {
                if (shown.value) {
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
                    )
                }
            }
        }
    }
    waitForIdle()
    return nav
}

/**
 * Builds the D19 example data: Mara's three Monday classes, created out of time order so the list
 * has to sort them, with Lunch Express cancelled on Mon 28 Sep only. The clock stays on Mon 28 Sep, 9:00 AM.
 */
fun Flow4Harness.seedD19(): Flow5Example {
    val mara = mara()
    val coreBurn = saveClass("TRX Core Burn", listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), slot(17, 30, 18, 30))
    val lunchExpress = saveClass(
        "Lunch Express TRX",
        listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
        slot(12, 15, 12, 45),
    )
    val sunrise = saveClass(
        "Sunrise TRX Strength",
        listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
        slot(6, 0, 6, 45),
    )
    CancelDate(classes, clock, timeZone).invoke(lunchExpress.id, MON_28_SEP_2026)
    return Flow5Example(mara = mara, sunrise = sunrise, lunchExpress = lunchExpress, coreBurn = coreBurn)
}

/** Signs in as Mara, mounts the app, and opens the QR tab. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.openDailyQr(world: Flow4Harness, lifecycle: HostLifecycle = HostLifecycle()): NavHostController {
    world.signInAsMara()
    val nav = mountFlow5(world, lifecycle)
    openCoachTab("qr")
    return nav
}

/** The one text under [tagName], exactly (a tag on a single Text node). */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertTextIs(tagName: String, expected: String) {
    assertEquals(listOf(expected), textsUnder(tagName).filter { it.isNotBlank() }, "text under $tagName")
}

/** Nodes showing exactly [text] inside the node tagged [parentTag]. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.countTextUnder(parentTag: String, text: String): Int =
    onAllNodes(hasText(text) and hasAnyAncestor(hasTestTag(parentTag)), useUnmergedTree = true)
        .fetchSemanticsNodes(atLeastOneRootRequired = false).size

/** The top edge of the node showing exactly [text] inside [parentTag]. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.topOfTextUnder(parentTag: String, text: String): Float {
    val nodes = onAllNodes(hasText(text) and hasAnyAncestor(hasTestTag(parentTag)), useUnmergedTree = true)
        .fetchSemanticsNodes(atLeastOneRootRequired = false)
    assertTrue(nodes.isNotEmpty(), "missing \"$text\" under $parentTag")
    return nodes.minOf { it.boundsInRoot.top }
}

/** Every top edge of a node showing exactly [text] inside [parentTag], top to bottom. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.topsOfTextUnder(parentTag: String, text: String): List<Float> =
    onAllNodes(hasText(text) and hasAnyAncestor(hasTestTag(parentTag)), useUnmergedTree = true)
        .fetchSemanticsNodes(atLeastOneRootRequired = false)
        .map { it.boundsInRoot.top }
        .sorted()

/** No progress indicator anywhere (spec decision 11: there is no loading spinner). */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertNoSpinner() {
    val spinners = onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo), useUnmergedTree = true)
        .fetchSemanticsNodes(atLeastOneRootRequired = false)
    assertTrue(spinners.isEmpty(), "no loading spinner")
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertNoDialogShown() {
    assertTrue(onAllNodes(isDialog()).fetchSemanticsNodes(atLeastOneRootRequired = false).isEmpty(), "no dialog")
}

/** A picture of the whole window, for the always-dark colour checks (F5-AC4). */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.screenPixels(): PixelMap = onRoot().captureToImage().toPixelMap()

fun PixelMap.argbAt(x: Float, y: Float): Int = this[x.toInt(), y.toInt()].toArgb()

fun Color.argb(): Int = toArgb()
