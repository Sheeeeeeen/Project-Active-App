package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import ph.project.active.domain.model.TimeSlot
import ph.project.active.domain.model.Visibility
import ph.project.active.ui.testing.Flow2Harness
import ph.project.active.ui.testing.Flow2Ui
import ph.project.active.ui.testing.ShowGuestList
import ph.project.active.ui.testing.tag

@OptIn(ExperimentalTestApi::class)
class F2GuestListScreenTest {
    @Test
    fun F2_AC1_listShowsPublicOpenClassesInCreationOrder() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val first = world.coreBurn()
            val second = world.saveClass(name = "Lunch Express TRX")
            setContent { ShowGuestList(world.guestList()) }
            tag("guest_list_title").assertTextContains("Classes")
            tag("guest_list_title").assertTextContains("Public TRX classes. No account needed.")
            tag("guest_list_count").assertTextContains("2 classes")
            onNodeWithText("PUBLIC CLASSES").assertExists()
            tag("guest_list_card_${first.id}").assertTextContains("TRX Core Burn")
            tag("guest_list_card_${first.id}").assertTextContains("Coach Mara Santos")
            tag("guest_list_card_${first.id}").assertTextContains("Mon \u00B7 Wed")
            tag("guest_list_card_${first.id}").assertTextContains("5:30 PM \u2013 6:30 PM")
            tag("guest_list_card_${second.id}").assertTextContains("Lunch Express TRX")
            val cards = onAllNodesWithText("Coach Mara Santos", substring = true).fetchSemanticsNodes()
            assertEquals(2, cards.size)
            val firstTop = tag("guest_list_card_${first.id}").fetchSemanticsNode().boundsInRoot.top
            val secondTop = tag("guest_list_card_${second.id}").fetchSemanticsNode().boundsInRoot.top
            assertTrue(firstTop < secondTop)
        }
    }

    @Test
    fun F2_AC2_privateCancelledAndEndedClassesStayOffTheList() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val open = world.coreBurn()
            val privateClass = world.saveClass(name = "Private", visibility = Visibility.PRIVATE)
            val cancelled = world.saveClass(name = "Cancelled")
            world.cancel(cancelled.id)
            world.setToday(kotlinx.datetime.LocalDate(2026, 10, 31))
            val ended = world.saveClass(name = "Ended", endDate = kotlinx.datetime.LocalDate(2026, 10, 31))
            world.setToday(kotlinx.datetime.LocalDate(2026, 11, 1))
            val endingToday = world.coreBurn(endDate = kotlinx.datetime.LocalDate(2026, 11, 1))
            setContent { ShowGuestList(world.guestList()) }
            tag("guest_list_card_${open.id}").assertDoesNotExist()
            tag("guest_list_card_${privateClass.id}").assertDoesNotExist()
            tag("guest_list_card_${cancelled.id}").assertDoesNotExist()
            tag("guest_list_card_${ended.id}").assertDoesNotExist()
            tag("guest_list_card_${endingToday.id}").assertExists()
        }
    }

    @Test
    fun F2_AC3_countReadsOneClassOrNClasses() = Flow2Ui.run {
        Flow2Harness().use { world ->
            world.coreBurn()
            setContent { ShowGuestList(world.guestList()) }
            tag("guest_list_count").assertTextContains("1 class")
        }
        Flow2Harness().use { world ->
            repeat(5) { index -> world.saveClass(name = "Class $index") }
            setContent { ShowGuestList(world.guestList()) }
            tag("guest_list_count").assertTextContains("5 classes")
        }
    }

    @Test
    fun F2_AC4_emptyListKeepsRegisterAndHidesTheCount() = Flow2Ui.run {
        Flow2Harness().use { world ->
            setContent { ShowGuestList(world.guestList()) }
            tag("guest_list_empty").assertTextContains("No public classes yet")
            tag("guest_list_empty").assertTextContains("When a coach creates a public class, it shows up here.")
            tag("guest_list_count").assertDoesNotExist()
            tag("guest_home_register_button").assertTextContains("Register or sign in")
        }
    }

    @Test
    fun F2_AC5_twoSlotsShareOneSortedLine() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val morning = TimeSlot(LocalTime(7, 0), LocalTime(8, 0))
            val evening = TimeSlot(LocalTime(17, 30), LocalTime(18, 30))
            val trxClass = world.saveClass(
                name = "TRX Core Burn",
                slots = mapOf(DayOfWeek.MONDAY to listOf(evening, morning)),
            )
            setContent { ShowGuestList(world.guestList()) }
            tag("guest_list_card_${trxClass.id}").assertTextContains("7:00 AM \u2013 8:00 AM, 5:30 PM \u2013 6:30 PM")
        }
    }

    @Test
    fun F2_AC6_offlineBannerKeepsTheListAndRegisterEnabled() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val trxClass = world.coreBurn()
            world.network.set(ph.project.active.domain.model.NetworkStatus.OFFLINE)
            setContent { ShowGuestList(world.guestList()) }
            tag("offline_banner").assertTextContains("No internet connection")
            tag("guest_list_card_${trxClass.id}").assertExists()
            tag("guest_home_register_button").assertIsEnabled()
        }
    }
}
