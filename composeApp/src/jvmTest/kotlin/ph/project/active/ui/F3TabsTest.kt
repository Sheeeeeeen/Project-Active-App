package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.ui.testing.Flow3Harness
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.mountFlow3
import ph.project.active.ui.testing.openTab
import ph.project.active.ui.testing.runFlow3UiTest
import ph.project.active.ui.testing.tag

@OptIn(ExperimentalTestApi::class)
class F3TabsTest {
    @Test
    fun F3_AC18_fourTabsInOrderAndOnlyAttendAndSessionsWearThePlaceholderTag() = runFlow3UiTest {
        Flow3Harness().use { world ->
            world.seedC1()
            world.signInAsPaolo()
            mountFlow3(world)

            val tabs = listOf("classes" to "Classes", "attend" to "Attend", "sessions" to "Sessions", "invites" to "Invites")
            for ((name, label) in tabs) assertTextUnder("student_tab_$name", label)
            val lefts = tabs.map { (name, _) -> leftOf("student_tab_$name") }
            assertEquals(lefts.sorted(), lefts, "tabs run Classes, Attend, Sessions, Invites")
            assertEquals(0, countTagged("student_tab_requests"))

            assertEquals(0, countTagged("placeholder_tag"), "Classes has no placeholder tag")

            openTab("attend")
            tag("student_tab_attend").assertIsSelected()
            assertTextUnder("student_attend", "Attend")
            assertEquals(1, countTagged("placeholder_tag"))

            openTab("sessions")
            tag("student_tab_sessions").assertIsSelected()
            assertTextUnder("student_sessions", "Sessions")
            assertEquals(1, countTagged("placeholder_tag"))

            openTab("invites")
            assertEquals(0, countTagged("placeholder_tag"), "Invites has no placeholder tag")
        }
    }

    @Test
    fun F3_AC20_theInvitesBadgeIsHiddenAtZeroAndShowsTheCount() = runFlow3UiTest {
        Flow3Harness().use { world ->
            world.signInAsPaolo()
            mountFlow3(world)
            tag("student_tab_invites_badge").assertDoesNotExist()
        }
        Flow3Harness().use { world ->
            world.seedC2()
            world.signInAsPaolo()
            mountFlow3(world)
            assertTextUnder("student_tab_invites_badge", "2")
        }
    }

    private fun androidx.compose.ui.test.ComposeUiTest.leftOf(tagName: String): Float =
        onNodeWithTag(tagName, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.left
}
