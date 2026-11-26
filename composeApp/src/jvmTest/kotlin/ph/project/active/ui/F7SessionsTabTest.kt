package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.TrxClass
import ph.project.active.ui.studenthome.StudentTabBarTestTags
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.MON_28_SEP_2026
import ph.project.active.ui.testing.assertNoProfileIcon
import ph.project.active.ui.testing.assertNoSpinner
import ph.project.active.ui.testing.assertNoTextUnder
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.cardIdsTopToBottom
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.openSessions
import ph.project.active.ui.testing.runFlow7UiTest
import ph.project.active.ui.testing.seedFlow7
import ph.project.active.ui.testing.semanticsValue
import ph.project.active.ui.testing.sessionIdOn
import ph.project.active.ui.testing.sessionsRowTag
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.topOf

/** Flow 7 C5 and C5b: the student's real Sessions tab on the default clock (F7-AC36 to F7-AC38). */
@OptIn(ExperimentalTestApi::class)
class F7SessionsTabTest {

    // F7-AC36: "Selecting Sessions shows the real screen under StudentTabBarTestTags.sessionsRoot: title "Sessions", sessions_overline "TRX ATTENDANCE", sessions_subtitle "Your dates for joined classes"."
    @Test
    fun F7_AC36_sessionsShowsTheRealScreenHeader() = runFlow7UiTest {
        Flow4Harness().use { world ->
            openSessions(world, world.seedFlow7())

            tag(StudentTabBarTestTags.sessionsRoot).assertExists()
            tag(StudentTabBarTestTags.sessions).assertIsSelected()
            assertTextIs(StudentTabBarTestTags.sessionsTitle, "Sessions")
            assertTextIs("sessions_overline", "TRX ATTENDANCE")
            assertTextIs("sessions_subtitle", "Your dates for joined classes")
        }
    }

    // F7-AC36: "No placeholder_tag, no profile icon, no spinner."
    @Test
    fun F7_AC36_noPlaceholderNoProfileIconNoSpinner() = runFlow7UiTest {
        Flow4Harness().use { world ->
            openSessions(world, world.seedFlow7())

            assertEquals(0, countTagged("placeholder_tag"))
            assertNoProfileIcon()
            assertNoSpinner()
        }
    }

    // F7-AC37: "sessions_group_upcoming "Upcoming" lists the rows in the F7-AC12 order … sessions_group_today "Today · Mon 28 Sep" lists Mobility Flow, Core Burn, Sunrise, then Lunch Express. There is no sessions_group_earlier."
    @Test
    fun F7_AC37_upcomingThenTodayInTheirOrder() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openSessions(world, example)

            assertTextUnder("sessions_group_upcoming", "Upcoming")
            assertTextUnder("sessions_group_today", "Today \u00B7 Mon 28 Sep")
            assertEquals(0, countTagged("sessions_group_earlier"))

            val upcoming = listOf(
                world.sessionIdOn(example.lunchExpress, TUE_29),
                world.sessionIdOn(example.sunrise, WED_30),
                world.sessionIdOn(example.coreBurn, WED_30),
                world.sessionIdOn(example.lunchExpress, OCT_1),
                world.sessionIdOn(example.mobilityFlow, OCT_1),
                world.sessionIdOn(example.sunrise, OCT_2),
                world.sessionIdOn(example.sunrise, OCT_5),
                world.sessionIdOn(example.lunchExpress, OCT_5),
                world.sessionIdOn(example.coreBurn, OCT_5),
                world.sessionIdOn(example.mobilityFlow, OCT_5),
            )
            val today = listOf(example.mobilityFlow, example.coreBurn, example.sunrise, example.lunchExpress)
                .map { world.sessionIdOn(it, MON_28_SEP_2026) }
            assertEquals(upcoming + today, cardIdsTopToBottom("sessions_row_"))

            val upcomingTop = topOf("sessions_group_upcoming")
            val todayTop = topOf("sessions_group_today")
            assertTrue(upcoming.all { topOf(sessionsRowTag(it)) > upcomingTop && topOf(sessionsRowTag(it)) < todayTop }, "Upcoming rows sit in the Upcoming group")
            assertTrue(today.all { topOf(sessionsRowTag(it)) > todayTop }, "Today rows sit in the Today group")
        }
    }

    // F7-AC37: "each with "Upcoming" and, for example, "Session 2 · 12:15 PM" for Tue 29 Lunch Express."
    @Test
    fun F7_AC37_upcomingRowsShowUpcomingAndTheirSessionLine() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openSessions(world, example)

            val lunch = sessionsRowTag(world.sessionIdOn(example.lunchExpress, TUE_29))
            assertTextUnder(lunch, "Lunch Express TRX")
            assertTextUnder(lunch, "Session 2 \u00B7 12:15 PM")
            assertTextUnder(lunch, "Upcoming")
            val mobility = sessionsRowTag(world.sessionIdOn(example.mobilityFlow, OCT_5))
            assertTextUnder(mobility, "Session 3 \u00B7 7:00 PM")
            assertTextUnder(mobility, "Upcoming")
        }
    }

    // F7-AC37: "Mobility Flow ("Upcoming"), Core Burn ("Upcoming"), Sunrise ("Present")"
    @Test
    fun F7_AC37_todayRowsShowTheirStatus() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openSessions(world, example)

            assertStatus(world, example.mobilityFlow, MON_28_SEP_2026, "Upcoming")
            assertStatus(world, example.coreBurn, MON_28_SEP_2026, "Upcoming")
            assertStatus(world, example.sunrise, MON_28_SEP_2026, "Present")
            assertTextUnder(sessionsRowTag(world.sessionIdOn(example.sunrise, MON_28_SEP_2026)), "TODAY")
        }
    }

    // F7-AC37: "The date tiles read "MON" / "28" / "SEP"."
    @Test
    fun F7_AC37_theDateTilesReadMon28Sep() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openSessions(world, example)

            for (trxClass in listOf(example.mobilityFlow, example.coreBurn, example.sunrise, example.lunchExpress)) {
                val row = sessionsRowTag(world.sessionIdOn(trxClass, MON_28_SEP_2026))
                assertTextUnder(row, "MON")
                assertTextUnder(row, "28")
                assertTextUnder(row, "SEP")
            }
            val wednesday = sessionsRowTag(world.sessionIdOn(example.sunrise, WED_30))
            assertTextUnder(wednesday, "WED")
            assertTextUnder(wednesday, "30")
        }
    }

    // F7-AC38: "the Lunch Express row is last: it shows "Session 1 · 12:15 PM" and a "Cancelled" chip, and carries sessions_row_cancelled = true; it has no "Absent" chip"
    @Test
    fun F7_AC38_todaysCancelledLunchIsLastAndMarkedCancelled() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openSessions(world, example)

            val lunchId = world.sessionIdOn(example.lunchExpress, MON_28_SEP_2026)
            val row = sessionsRowTag(lunchId)
            assertEquals(lunchId, cardIdsTopToBottom("sessions_row_").last())
            assertTextUnder(row, "Session 1 \u00B7 12:15 PM")
            assertTextUnder(row, "Cancelled")
            assertNoTextUnder(row, "Absent")
            assertEquals(true, semanticsValue(row, "sessions_row_cancelled"))
        }
    }

    // F7-AC38: "Other rows do not carry sessions_row_cancelled = true." (the key marks the cancelled row only)
    @Test
    fun F7_AC38_otherRowsAreNotMarkedCancelled() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openSessions(world, example)

            for (trxClass in listOf(example.mobilityFlow, example.coreBurn, example.sunrise)) {
                val row = sessionsRowTag(world.sessionIdOn(trxClass, MON_28_SEP_2026))
                assertTrue(semanticsValue(row, "sessions_row_cancelled") != true, "$row is not cancelled")
            }
        }
    }

    // F7-AC38: "no "Absent" chip … after the clock moves to Tue 29 (when it sits in Earlier, still Cancelled)"
    @Test
    fun F7_AC38_onTuesdayTheCancelledLunchSitsInEarlierStillCancelled() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            world.setNow(TUE_29, 9, 0)
            openSessions(world, example)

            val row = sessionsRowTag(world.sessionIdOn(example.lunchExpress, MON_28_SEP_2026))
            assertTrue(topOf(row) > topOf("sessions_group_earlier"), "Mon 28 Lunch sits in Earlier")
            assertTextUnder(row, "Cancelled")
            assertNoTextUnder(row, "Absent")
            assertEquals(true, semanticsValue(row, "sessions_row_cancelled"))
        }
    }

    // F7-AC38: "the Tue 29 Lunch Express row in Upcoming shows "Session 2 · 12:15 PM"."
    @Test
    fun F7_AC38_tuesdaysLunchKeepsSessionTwo() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openSessions(world, example)

            val row = sessionsRowTag(world.sessionIdOn(example.lunchExpress, TUE_29))
            assertTrue(topOf(row) < topOf("sessions_group_today"), "Tue 29 Lunch sits in Upcoming")
            assertTextUnder(row, "Session 2 \u00B7 12:15 PM")
            assertNoTextUnder(row, "Cancelled")
        }
    }
}

private val TUE_29 = LocalDate(2026, 9, 29)
private val WED_30 = LocalDate(2026, 9, 30)
private val OCT_1 = LocalDate(2026, 10, 1)
private val OCT_2 = LocalDate(2026, 10, 2)
private val OCT_5 = LocalDate(2026, 10, 5)

@OptIn(ExperimentalTestApi::class)
private fun androidx.compose.ui.test.ComposeUiTest.assertStatus(world: Flow4Harness, trxClass: TrxClass, date: LocalDate, status: String) {
    assertTextUnder(sessionsRowTag(world.sessionIdOn(trxClass, date)), status)
}
