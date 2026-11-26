package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.DayOfWeek
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.assertNoSpinner
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.assertTextShown
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.countTaggedUnder
import ph.project.active.ui.testing.countTextUnder
import ph.project.active.ui.testing.openDailyQr
import ph.project.active.ui.testing.runFlow5UiTest
import ph.project.active.ui.testing.seedD19
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.topOfTextUnder
import ph.project.active.ui.testing.topsOfTextUnder

/** Flow 5 D19: the coach's real Daily QR screen (F5-AC1, F5-AC2). */
@OptIn(ExperimentalTestApi::class)
class F5DailyQrScreenTest {

    // F5-AC1: "coach_qr exists; coach_qr_title reads "Today’s code"; the eyebrow is "DAILY QR"; the subtitle is "Mon 28 Sep 2026 · Coach Mara Santos""
    @Test
    fun F5_AC1_theQrTabShowsTheRealDailyQrHeader() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            openDailyQr(world)

            tag("coach_qr").assertExists()
            assertTextIs("coach_qr_title", "Today\u2019s code")
            assertTextIs("coach_qr_overline", "DAILY QR")
            assertTextIs("coach_qr_subtitle", "Mon 28 Sep 2026 \u00B7 Coach Mara Santos")
        }
    }

    // F5-AC1: "the tab bar is visible with coach_tab_qr selected"
    @Test
    fun F5_AC1_theTabBarShowsWithQrSelected() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            openDailyQr(world)

            tag("coach_tab_classes").assertExists()
            tag("coach_tab_attendance").assertExists()
            tag("coach_tab_qr").assertIsSelected()
        }
    }

    // F5-AC1: "There is no placeholder_tag under coach_qr (or anywhere on the QR tab). There is no coach_qr_account and no add button. There is no loading spinner."
    @Test
    fun F5_AC1_noPlaceholderNoAccountNoAddButtonAndNoSpinner() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            openDailyQr(world)

            assertEquals(0, countTaggedUnder("coach_qr", "placeholder_tag"), "no placeholder under coach_qr")
            assertEquals(0, countTagged("placeholder_tag"), "no placeholder anywhere on the QR tab")
            tag("coach_qr_account").assertDoesNotExist()
            tag("coach_qr_add").assertDoesNotExist()
            tag("coach_classes_add").assertDoesNotExist()
            assertNoSpinner()
        }
    }

    // F5-AC2: "the card shows "Valid Mon 28 Sep 2026 only", the brightness caption "Brightness is raised while this code is open""
    @Test
    fun F5_AC2_theCardShowsTheValidLineAndTheBrightnessCaption() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            openDailyQr(world)

            tag("coach_qr_card").assertExists()
            assertTextIs("coach_qr_valid", "Valid Mon 28 Sep 2026 only")
            assertTextIs("coach_qr_brightness", "Brightness is raised while this code is open")
        }
    }

    // F5-AC2: "the section "This code covers today" / "One code · all classes""
    @Test
    fun F5_AC2_theSectionHeaderSaysOneCodeCoversToday() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            openDailyQr(world)

            assertTextShown("This code covers today")
            assertTextShown("One code \u00B7 all classes")
        }
    }

    // F5-AC2: "the three session rows in that order, each with formatSlot and the Scheduled or Cancelled chip. Cancelled Lunch Express is present."
    @Test
    fun F5_AC2_todaysThreeSessionsShowInStartTimeOrderWithSlotAndChip() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            openDailyQr(world)

            val names = listOf("Sunrise TRX Strength", "Lunch Express TRX", "TRX Core Burn")
            val slots = listOf("6:00 AM \u2013 6:45 AM", "12:15 PM \u2013 12:45 PM", "5:30 PM \u2013 6:30 PM")
            val nameTops = names.map { topOfTextUnder("coach_qr_sessions", it) }
            val slotTops = slots.map { topOfTextUnder("coach_qr_sessions", it) }
            assertEquals(nameTops.sorted(), nameTops, "Sunrise, Lunch Express, Core Burn from top to bottom")
            assertEquals(slotTops.sorted(), slotTops, "each row's slot follows the same order")

            assertEquals(2, countTextUnder("coach_qr_sessions", "Scheduled"))
            assertEquals(1, countTextUnder("coach_qr_sessions", "Cancelled"))
            val cancelledTop = topOfTextUnder("coach_qr_sessions", "Cancelled")
            assertEquals("Lunch Express TRX", names[nameTops.indexOfNearest(cancelledTop)], "the Cancelled chip is on Lunch Express")
            val scheduledRows = topsOfTextUnder("coach_qr_sessions", "Scheduled").map { names[nameTops.indexOfNearest(it)] }
            assertEquals(listOf("Sunrise TRX Strength", "TRX Core Burn"), scheduledRows, "the Scheduled chips are on Sunrise and Core Burn")
        }
    }

    // F5-AC2 / spec assumption 3: session rows are asserted by text under coach_qr_sessions; nothing else is listed
    @Test
    fun F5_AC2_onlyTodaysSessionsAreListed() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            world.saveClass("TRX Tuesday Only", listOf(DayOfWeek.TUESDAY), world.slot(7, 0, 8, 0))
            openDailyQr(world)

            assertEquals(0, countTextUnder("coach_qr_sessions", "TRX Tuesday Only"))
            assertEquals(1, countTextUnder("coach_qr_sessions", "Sunrise TRX Strength"))
        }
    }

    private fun List<Float>.indexOfNearest(top: Float): Int = indices.minBy { abs(this[it] - top) }
}
