package ph.project.active.ui.testing

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.navigation.NavHostController
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Runs a flow 3 UI test in a tall window, so all six C1 cards are composed at once.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
fun runFlow3UiTest(block: suspend ComposeUiTest.() -> Unit) {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    try {
        runDesktopComposeUiTest(width = 480, height = 2400) { block() }
    } finally {
        Dispatchers.resetMain()
    }
}

/** Mounts the one NavHost with the flow 3 world's factories. Call `signInAsPaolo()` first to start on StudentHome. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.mountFlow3(world: Flow3Harness): NavHostController {
    lateinit var nav: NavHostController
    setContent {
        Flow1Compose.Nav(
            rootViewModel = world.root(),
            guestClassListViewModel = world.guest(),
            registrationViewModel = world::registration,
            classDetailViewModel = world::detail,
            studentClassesViewModel = world::studentClasses,
            invitesViewModel = world::invites,
            onNavController = { nav = it },
        )
    }
    waitForIdle()
    return nav
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.openTab(name: String) {
    tag("student_tab_$name").performClick()
    waitForIdle()
}

/** Every text under a tagged node, its children included, so a check doesn't depend on how a card merges. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.textUnder(tagName: String): String =
    textsOf(onNodeWithTag(tagName, useUnmergedTree = true).fetchSemanticsNode()).joinToString("\n")

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertTextUnder(tagName: String, expected: String) {
    val actual = textUnder(tagName)
    assertTrue(expected in actual, "expected \"$expected\" under $tagName, found:\n$actual")
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertNoTextUnder(tagName: String, unexpected: String) {
    val actual = textUnder(tagName)
    assertTrue(unexpected !in actual, "did not expect \"$unexpected\" under $tagName, found:\n$actual")
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.topOf(tagName: String): Float =
    onNodeWithTag(tagName, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.top

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.countTagged(tagName: String): Int =
    onAllNodesWithTag(tagName, useUnmergedTree = true).fetchSemanticsNodes(atLeastOneRootRequired = false).size

/** Tags of student class cards that are on screen, top to bottom. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.cardIdsTopToBottom(prefix: String): List<String> =
    onAllNodes(hasTestTagPrefix(prefix), useUnmergedTree = true)
        .fetchSemanticsNodes(atLeastOneRootRequired = false)
        .sortedBy { it.boundsInRoot.top }
        .map { it.config[SemanticsProperties.TestTag].removePrefix(prefix) }
        .distinct()

private fun textsOf(node: SemanticsNode): List<String> {
    val own = node.config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text } +
        node.config.getOrNull(SemanticsProperties.EditableText)?.text.let { listOfNotNull(it) }
    return own + node.children.flatMap { textsOf(it) }
}
