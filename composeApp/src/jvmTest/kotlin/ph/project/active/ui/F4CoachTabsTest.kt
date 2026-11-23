package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.performClick
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.cardIdsTopToBottom
import ph.project.active.ui.testing.coachRoutes
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.countTaggedUnder
import ph.project.active.ui.testing.mountFlow4
import ph.project.active.ui.testing.openCoachCard
import ph.project.active.ui.testing.openCoachTab
import ph.project.active.ui.testing.runFlow4UiTest
import ph.project.active.ui.testing.tag

@OptIn(ExperimentalTestApi::class)
class F4CoachTabsTest {
    @Test
    fun F4_AC25_theTabBarHasClassesQrAndAttendanceInOrder() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.seedD1()
            world.signInAsMara()
            mountFlow4(world)

            val tabs = cardIdsTopToBottom("coach_tab_")
            assertEquals(setOf("classes", "qr", "attendance"), tabs.toSet())
            val lefts = listOf("classes", "qr", "attendance").map {
                tag("coach_tab_$it").fetchSemanticsNode().boundsInRoot.left
            }
            assertEquals(lefts.sorted(), lefts, "Classes, QR, Attendance from left to right")
            assertTextUnder("coach_tab_classes", "Classes")
            assertTextUnder("coach_tab_qr", "QR")
            assertTextUnder("coach_tab_attendance", "Attendance")
        }
    }

    @Test
    fun F4_AC25_qrAndAttendanceShowTheirTitleAndAPlaceholderTag() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.seedD1()
            world.signInAsMara()
            val nav = mountFlow4(world)

            openCoachTab("qr")
            tag("coach_tab_qr").assertIsSelected()
            tag("coach_qr").assertExists()
            assertTextUnder("coach_qr", "QR")
            assertEquals(1, countTaggedUnder("coach_qr", "placeholder_tag"), "the QR screen's own placeholder")
            assertEquals(2, countTagged("placeholder_tag"), "plus the account button's")

            openCoachTab("attendance")
            tag("coach_tab_attendance").assertIsSelected()
            tag("coach_attendance").assertExists()
            assertTextUnder("coach_attendance", "Attendance")
            assertEquals(1, countTaggedUnder("coach_attendance", "placeholder_tag"))
            assertEquals(2, countTagged("placeholder_tag"))

            openCoachTab("classes")
            assertEquals(listOf("CoachHome"), coachRoutes(nav), "switching tabs does not change the back stack")
        }
    }

    @Test
    fun F4_AC25_classesCreateAndManageBodiesHaveNoPlaceholderTag() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            world.signInAsMara()
            mountFlow4(world)

            assertEquals(1, countTagged("placeholder_tag"), "only the account button's on Classes")
            assertEquals(1, countTaggedUnder("coach_classes_account", "placeholder_tag"))

            tag("coach_classes_add").performClick()
            waitForIdle()
            tag("create_class_title").assertExists()
            assertEquals(0, countTagged("placeholder_tag"), "Create has none")
            tag("create_class_cancel").performClick()
            waitForIdle()

            openCoachCard(d1.coreBurn.id)
            tag("manage_class_back").assertExists()
            assertEquals(0, countTagged("placeholder_tag"), "Manage has none")
        }
    }
}
