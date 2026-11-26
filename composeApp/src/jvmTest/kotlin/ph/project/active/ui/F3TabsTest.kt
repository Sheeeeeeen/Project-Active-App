package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.ui.testing.Flow3Harness
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.countTaggedUnder
import ph.project.active.ui.testing.mountFlow3
import ph.project.active.ui.testing.openTab
import ph.project.active.ui.testing.runFlow3UiTest
import ph.project.active.ui.testing.tag

@OptIn(ExperimentalTestApi::class)
class F3TabsTest {
    @Test
    fun F3_AC18_fourTabsInOrderAndOnlyTheAccountButtonIsTaggedOnClassesAndInvites() = runFlow3UiTest {
        Flow3Harness().use { world ->
            world.seedC1()
            world.signInAsPaolo()
            mountFlow3(world)

            val tabs = listOf("classes" to "Classes", "attend" to "Attend", "sessions" to "Sessions", "invites" to "Invites")
            for ((name, label) in tabs) assertTextUnder("student_tab_$name", label)
            val lefts = tabs.map { (name, _) -> leftOf("student_tab_$name") }
            assertEquals(lefts.sorted(), lefts, "tabs run Classes, Attend, Sessions, Invites")
            assertEquals(0, countTagged("student_tab_requests"))

            assertEquals(1, countTagged("placeholder_tag"), "Classes has one placeholder tag")
            assertEquals(1, countTaggedUnder("student_classes_account", "placeholder_tag"), "and it is on the account button")

            openTab("attend")
            tag("student_tab_attend").assertIsSelected()
            assertTextUnder("student_attend", "Attend")
            // Flow 7 F7-AC15 / F7-AC47: Attend is the real screen now, with no placeholder tag and no profile icon.
            assertEquals(0, countTagged("placeholder_tag"), "Attend has no placeholder tag")

            openTab("sessions")
            tag("student_tab_sessions").assertIsSelected()
            assertTextUnder("student_sessions", "Sessions")
            // Flow 7 F7-AC36 / F7-AC47: Sessions is the real screen now, with no placeholder tag and no profile icon.
            assertEquals(0, countTagged("placeholder_tag"), "Sessions has no placeholder tag")

            openTab("invites")
            assertEquals(1, countTagged("placeholder_tag"), "Invites has one placeholder tag")
            assertEquals(1, countTaggedUnder("invites_account", "placeholder_tag"), "and it is on the account button")
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
