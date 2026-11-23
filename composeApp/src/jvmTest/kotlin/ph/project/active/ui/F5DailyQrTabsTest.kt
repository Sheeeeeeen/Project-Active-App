package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.coachRoutes
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.countTaggedUnder
import ph.project.active.ui.testing.openCoachTab
import ph.project.active.ui.testing.openDailyQr
import ph.project.active.ui.testing.runFlow5UiTest
import ph.project.active.ui.testing.seedD19
import ph.project.active.ui.testing.tag

/** Flow 5 F5-AC6: switching tabs from the real Daily QR keeps one back-stack entry. */
@OptIn(ExperimentalTestApi::class)
class F5DailyQrTabsTest {

    // F5-AC6: "From the QR tab, selecting Classes returns to the class list without changing the back stack."
    @Test
    fun F5_AC6_selectingClassesReturnsToTheListWithTheSameBackStack() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            val nav = openDailyQr(world)

            openCoachTab("classes")

            tag("coach_tab_classes").assertIsSelected()
            tag("coach_classes_title").assertExists()
            tag("coach_qr").assertDoesNotExist()
            assertEquals(listOf("CoachHome"), coachRoutes(nav))
        }
    }

    // F5-AC6: "Selecting Attendance shows the existing placeholder: coach_attendance exists, the title is "Attendance", one placeholder_tag is under coach_attendance, and one is under coach_attendance_account."
    @Test
    fun F5_AC6_selectingAttendanceShowsTheUnchangedPlaceholder() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            val nav = openDailyQr(world)

            openCoachTab("attendance")

            tag("coach_attendance").assertExists()
            assertTextIs("coach_attendance_title", "Attendance")
            assertEquals(1, countTaggedUnder("coach_attendance", "placeholder_tag"))
            assertEquals(1, countTaggedUnder("coach_attendance_account", "placeholder_tag"))
            assertEquals(listOf("CoachHome"), coachRoutes(nav))
        }
    }

    // F5-AC6: "Selecting QR again shows the real Daily QR screen, still with no placeholder_tag."
    @Test
    fun F5_AC6_selectingQrAgainShowsTheRealScreenWithNoPlaceholder() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            val nav = openDailyQr(world)
            openCoachTab("attendance")

            openCoachTab("qr")

            tag("coach_qr").assertExists()
            assertTextIs("coach_qr_title", "Today\u2019s code")
            assertEquals(0, countTagged("placeholder_tag"))
            assertEquals(listOf("CoachHome"), coachRoutes(nav))
        }
    }
}
