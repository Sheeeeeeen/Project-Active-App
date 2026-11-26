package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.coachRoutes
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.openCoachAttendance
import ph.project.active.ui.testing.openCoachTab
import ph.project.active.ui.testing.openDailyQr
import ph.project.active.ui.testing.runFlow5UiTest
import ph.project.active.ui.testing.seedD22
import ph.project.active.ui.testing.tag

/** Flow 6 F6-AC15: tabs around the real Attendance screen keep one back-stack entry. */
@OptIn(ExperimentalTestApi::class)
class F6AttendanceTabsTest {

    // F6-AC15: "From Attendance, selecting Classes returns to the class list … without changing the back stack."
    @Test
    fun F6_AC15_selectingClassesReturnsToTheListWithTheSameBackStack() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD22()
            val nav = openCoachAttendance(world)

            openCoachTab("classes")

            tag("coach_tab_classes").assertIsSelected()
            tag("coach_classes_title").assertExists()
            tag("coach_attendance").assertDoesNotExist()
            assertEquals(listOf("CoachHome"), coachRoutes(nav))
        }
    }

    // F6-AC15: "and selecting QR shows the real Daily QR screen, without changing the back stack"
    @Test
    fun F6_AC15_selectingQrShowsTheRealDailyQrWithTheSameBackStack() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD22()
            val nav = openCoachAttendance(world)

            openCoachTab("qr")

            tag("coach_qr").assertExists()
            assertTextIs("coach_qr_title", "Today\u2019s code")
            assertEquals(listOf("CoachHome"), coachRoutes(nav))
        }
    }

    // F6-AC15: "Selecting Attendance again shows the real Attendance screen, still with no placeholder_tag."
    @Test
    fun F6_AC15_selectingAttendanceAgainShowsTheRealScreenWithNoPlaceholder() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD22()
            val nav = openCoachAttendance(world)
            openCoachTab("qr")

            openCoachTab("attendance")

            tag("coach_attendance").assertExists()
            assertTextIs("coach_attendance_overline", "TRX ATTENDANCE")
            tag("coach_attendance_date").assertExists()
            assertEquals(0, countTagged("placeholder_tag"))
            assertEquals(listOf("CoachHome"), coachRoutes(nav))
        }
    }

    // F6-AC15: "Leaving QR for Attendance still restores brightness (F5-AC5 holds)."
    @Test
    fun F6_AC15_leavingQrForTheRealAttendanceScreenRestoresBrightness() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD22()
            openDailyQr(world)

            openCoachTab("attendance")

            tag("coach_attendance_date").assertExists()
            assertFalse(world.brightness.isRaised)
            assertEquals(listOf("raise", "restore"), world.brightness.calls)
        }
    }
}
