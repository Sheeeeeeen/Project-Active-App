package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.performClick
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.HostLifecycle
import ph.project.active.ui.testing.MON_28_SEP_2026
import ph.project.active.ui.testing.TUE_29_SEP_2026
import ph.project.active.ui.testing.assertNoTextUnder
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.cardIdsTopToBottom
import ph.project.active.ui.testing.countTextUnder
import ph.project.active.ui.testing.openCoachAttendance
import ph.project.active.ui.testing.openCoachTab
import ph.project.active.ui.testing.rowTag
import ph.project.active.ui.testing.runFlow5UiTest
import ph.project.active.ui.testing.scanAt
import ph.project.active.ui.testing.seedD22
import ph.project.active.ui.testing.tag

/** Flow 6 decisions 10 and 11: live updates, reload on tab show / foreground, and overnight (F6-AC11 to F6-AC13). */
@OptIn(ExperimentalTestApi::class)
class F6LiveUpdateAndRefreshTest {

    // F6-AC11: "adding a record for Dana on Core Burn at 5:33 PM to the store (no tab switch, no other call) changes her row to "Chose TRX Core Burn · scanned 5:33 PM" with a "Present" chip, and the Present count becomes "4""
    @Test
    fun F6_AC11_aNewScanInTheStoreShowsWhileTheTabIsOpen() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val d22 = world.seedD22()
            openCoachAttendance(world)
            val dana = rowTag(d22.coreBurnSessionId, d22.dana)
            assertEquals(1, countTextUnder(dana, "Not scanned yet"))

            runOnIdle { world.scanAt(d22.coreBurn, d22.dana, MON_28_SEP_2026, 17, 33) }
            waitForIdle()

            assertEquals(1, countTextUnder(dana, "Chose TRX Core Burn \u00B7 scanned 5:33 PM"))
            assertEquals(1, countTextUnder(dana, "Present"))
            assertNoTextUnder(dana, "Not scanned yet")
            assertTextIs("coach_attendance_present_count", "4")
        }
    }

    // F6-AC12: "Leaving the Attendance tab for Classes, adding a record for Gia on Core Burn, then selecting Attendance again shows Gia as Present."
    @Test
    fun F6_AC12_aScanAddedWhileOnAnotherTabShowsWhenAttendanceIsSelectedAgain() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val d22 = world.seedD22()
            openCoachAttendance(world)
            openCoachTab("classes")

            world.scanAt(d22.coreBurn, d22.gia, MON_28_SEP_2026, 17, 38)
            openCoachTab("attendance")

            val gia = rowTag(d22.coreBurnSessionId, d22.gia)
            assertEquals(1, countTextUnder(gia, "Present"))
            assertEquals(1, countTextUnder(gia, "Chose TRX Core Burn \u00B7 scanned 5:38 PM"))
        }
    }

    // Decision 10: "The screen also reloads each time the Attendance tab is shown" (assumption 6: a roster change is not live, so only the reload can show it)
    @Test
    fun F6_AC12_showingTheTabAgainReloadsARosterChange() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val d22 = world.seedD22()
            openCoachAttendance(world)
            openCoachTab("classes")

            world.joinByRequest(d22.coreBurn.id, d22.hana)
            openCoachTab("attendance")

            assertEquals(
                listOf(d22.paolo.id, d22.dana.id, d22.gia.id, d22.hana.id),
                cardIdsTopToBottom("coach_attendance_row_${d22.coreBurnSessionId}_"),
            )
        }
    }

    // F6-AC12: "Sending the app to the background and back (HostLifecycle) also reloads."
    @Test
    fun F6_AC12_comingBackToTheForegroundReloads() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val d22 = world.seedD22()
            val lifecycle = HostLifecycle()
            openCoachAttendance(world, lifecycle)

            runOnIdle { lifecycle.background() }
            waitForIdle()
            world.joinByRequest(d22.coreBurn.id, d22.hana)
            runOnIdle { lifecycle.foreground() }
            waitForIdle()

            assertEquals(
                listOf(d22.paolo.id, d22.dana.id, d22.gia.id, d22.hana.id),
                cardIdsTopToBottom("coach_attendance_row_${d22.coreBurnSessionId}_"),
            )
            assertEquals(1, countTextUnder(rowTag(d22.coreBurnSessionId, d22.hana), "Not scanned yet"))
        }
    }

    // F6-AC13: "With the tab showing today (Mon 28 Sep), move the clock to Tue 29 Sep and bring the app back to the foreground: the date card shows "Tue 29 Sep 2026" / "Today · in progress"."
    @Test
    fun F6_AC13_overnightTheForegroundMovesTodayToTheNewDay() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD22()
            val lifecycle = HostLifecycle()
            openCoachAttendance(world, lifecycle)
            assertTextIs("coach_attendance_date", "Mon 28 Sep 2026")

            runOnIdle { lifecycle.background() }
            waitForIdle()
            world.setNow(TUE_29_SEP_2026, 9, 41)
            runOnIdle { lifecycle.foreground() }
            waitForIdle()

            assertTextIs("coach_attendance_date", "Tue 29 Sep 2026")
            assertTextIs("coach_attendance_day_state", "Today \u00B7 in progress")
        }
    }

    // F6-AC13: "If the coach had paged back to Sun 27 Sep first, the same steps leave it on "Sun 27 Sep 2026"."
    @Test
    fun F6_AC13_overnightAPagedBackDateStays() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD22()
            val lifecycle = HostLifecycle()
            openCoachAttendance(world, lifecycle)
            tag("coach_attendance_prev_day").performClick()
            waitForIdle()
            assertTextIs("coach_attendance_date", "Sun 27 Sep 2026")

            runOnIdle { lifecycle.background() }
            waitForIdle()
            world.setNow(TUE_29_SEP_2026, 9, 41)
            runOnIdle { lifecycle.foreground() }
            waitForIdle()

            assertTextIs("coach_attendance_date", "Sun 27 Sep 2026")
            assertTextIs("coach_attendance_day_state", "Day ended")
        }
    }
}
