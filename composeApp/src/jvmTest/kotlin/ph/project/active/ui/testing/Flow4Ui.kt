package ph.project.active.ui.testing

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.project.active.ui.navigation.CoachHome
import ph.project.active.ui.navigation.CreateClass
import ph.project.active.ui.navigation.GuestHome
import ph.project.active.ui.navigation.ManageClass
import ph.project.active.ui.navigation.Registration

/** The two error lines a blocked action on a cancelled class may show (spec decision 15). */
val CANCELLED_CLASS_ERRORS = listOf("That action is not available.", "This class has been cancelled. Nothing was sent.")

const val OFFLINE_NOTHING_SAVED = "You\u2019re offline. Nothing was saved."

/** Runs a flow 4 UI test in the same tall window as flow 3, so a whole list or schedule is composed. */
@OptIn(ExperimentalTestApi::class)
fun runFlow4UiTest(block: suspend ComposeUiTest.() -> Unit) = runFlow3UiTest(block)

/** Mounts the one NavHost with the flow 4 world's factories. Call `signInAsMara()` first to start on CoachHome. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.mountFlow4(world: Flow4Harness): NavHostController {
    lateinit var nav: NavHostController
    setContent {
        Flow1Compose.Nav(
            rootViewModel = world.root(),
            guestClassListViewModel = world.guest(),
            registrationViewModel = world::registration,
            classDetailViewModel = world::detail,
            studentClassesViewModel = world::studentClasses,
            invitesViewModel = world::invites,
            coachClassesViewModel = world::coachClasses,
            createClassViewModel = world::createClass,
            manageClassViewModel = world::manageClass,
            dailyQrViewModel = world::dailyQr,
            screenBrightness = world.brightness,
            onNavController = { nav = it },
        )
    }
    waitForIdle()
    return nav
}

/** The coach back stack as readable names, for example `[CoachHome, ManageClass(class-1)]`. */
fun coachRoutes(nav: NavHostController): List<String> = nav.currentBackStack.value.mapNotNull { entry ->
    val destination = entry.destination
    when {
        destination.hasRoute(GuestHome::class) -> "GuestHome"
        destination.hasRoute(Registration::class) -> "Registration"
        destination.hasRoute(CoachHome::class) -> "CoachHome"
        destination.hasRoute(CreateClass::class) -> "CreateClass"
        destination.hasRoute(ManageClass::class) -> "ManageClass(${entry.toRoute<ManageClass>().classId})"
        else -> null
    }
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.openCoachTab(name: String) {
    tag("coach_tab_$name").performClick()
    waitForIdle()
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.openManageTab(name: String) {
    tag("manage_tab_$name").performClick()
    waitForIdle()
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.openCoachCard(classId: String) {
    tag("coach_class_card_$classId").performClick()
    waitForIdle()
}

/** Calls a node's click action directly, so two taps land before the first navigation is drawn. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.tapTwiceQuickly(tagName: String) {
    val node = onNodeWithTag(tagName, useUnmergedTree = true).fetchSemanticsNode()
    val click = checkNotNull(node.config[SemanticsActions.OnClick].action)
    click()
    click()
    waitForIdle()
}

/** Taps the one control that reads [text], for controls the spec gives no test tag (the class options action). */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.clickText(text: String) {
    onNodeWithText(text).performClick()
    waitForIdle()
}

/** Types an employee ID and submits it with the keyboard action (spec decision 14). */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.submitInviteSearch(query: String) {
    tag("manage_invite_search").performTextReplacement(query)
    tag("manage_invite_search").performImeAction()
    waitForIdle()
}

/** Section 10 Q7: the confirm dialog is always shown first (even offline or on a cancelled class); confirm it. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.confirmTheDialog() {
    onNodeWithTag("manage_dialog", useUnmergedTree = true).assertExists()
    onNodeWithTag("manage_dialog_confirm", useUnmergedTree = true).performClick()
    waitForIdle()
}

/** Passes when the texts under [tagName], joined either way, contain [expected] (a line may be split in two Text nodes). */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertLineUnder(tagName: String, expected: String) {
    val texts = textsUnder(tagName)
    val found = expected in texts.joinToString("\n") || expected in texts.joinToString("")
    assertTrue(found, "expected \"$expected\" under $tagName, found:\n${texts.joinToString("\n")}")
}

/** Asserts some on-screen text node reads exactly [text]. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertTextShown(text: String) {
    val found = onAllNodes(hasText(text), useUnmergedTree = true).fetchSemanticsNodes(atLeastOneRootRequired = false)
    assertTrue(found.isNotEmpty(), "missing text: $text")
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertTextNotShown(text: String, substring: Boolean = false) {
    val found = onAllNodes(hasText(text, substring = substring, ignoreCase = substring), useUnmergedTree = true)
        .fetchSemanticsNodes(atLeastOneRootRequired = false)
    assertTrue(found.isEmpty(), "did not expect text: $text")
}

/** The top edge of the first node showing exactly [text]. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.topOfText(text: String): Float {
    val nodes = onAllNodes(hasText(text), useUnmergedTree = true).fetchSemanticsNodes(atLeastOneRootRequired = false)
    assertTrue(nodes.isNotEmpty(), "missing text: $text")
    return nodes.minOf { it.boundsInRoot.top }
}

/** No node under [tagName] (the node itself included) has a click action. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertNothingClickableUnder(tagName: String) {
    val root = onNodeWithTag(tagName, useUnmergedTree = true).fetchSemanticsNode()
    assertEquals(0, clickables(root), "$tagName must not be tappable or dismissible")
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertNoUndo() {
    assertTextNotShown("Undo", substring = true)
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.textsUnder(tagName: String): List<String> =
    allTexts(onNodeWithTag(tagName, useUnmergedTree = true).fetchSemanticsNode())

private fun allTexts(node: SemanticsNode): List<String> {
    val own = node.config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text } +
        listOfNotNull(node.config.getOrNull(SemanticsProperties.EditableText)?.text)
    return own + node.children.flatMap { allTexts(it) }
}

private fun clickables(node: SemanticsNode): Int =
    (if (node.config.getOrNull(SemanticsActions.OnClick) != null) 1 else 0) + node.children.sumOf { clickables(it) }
