package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.coachRoutes
import ph.project.active.ui.testing.countTagged
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

    // F5-AC6: "Selecting Attendance shows … coach_attendance exists, the title is "Attendance"". Flow 6 (F6-AC1, F6-AC24)
    // replaced the placeholder with the real screen, so the placeholder_tag and coach_attendance_account clauses are gone.
    @Test
    fun F5_AC6_selectingAttendanceShowsTheAttendanceTab() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            val nav = openDailyQr(world)

            openCoachTab("attendance")

            tag("coach_attendance").assertExists()
            assertTextIs("coach_attendance_title", "Attendance")
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
