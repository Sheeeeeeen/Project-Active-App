package ph.project.active.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.performClick
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.SessionStatus
import ph.project.active.ui.testing.Flow4Example
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.assertNoTextUnder
import ph.project.active.ui.testing.assertNoUndo
import ph.project.active.ui.testing.assertTextNotShown
import ph.project.active.ui.testing.assertTextShown
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.cardIdsTopToBottom
import ph.project.active.ui.testing.mountFlow4
import ph.project.active.ui.testing.openCoachCard
import ph.project.active.ui.testing.openManageTab
import ph.project.active.ui.testing.runFlow4UiTest
import ph.project.active.ui.testing.tag

private val MON_5_OCT_2026 = LocalDate(2026, 10, 5)

/** Core Burn's ten dates (created Mon 28 Sep 2026, ends Sat 31 Oct 2026), in session-number order. */
private val CORE_BURN_DATES = listOf(
    "2026-09-28", "2026-09-30", "2026-10-05", "2026-10-07", "2026-10-12",
    "2026-10-14", "2026-10-19", "2026-10-21", "2026-10-26", "2026-10-28",
)

@OptIn(ExperimentalTestApi::class)
class F4ManageScheduleTest {
    @Test
    fun F4_AC16_manageHasNoControlThatEditsTheClass() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            openCoreBurn(world, d1)

            for (tab in listOf("schedule", "requests", "roster")) {
                openManageTab(tab)
                assertEquals(0, editableFields(), "the $tab tab has no text field")
                assertNoEditLabels()
            }
            openManageTab("invite")
            assertEquals(1, editableFields(), "Invite has only the employee ID field")
            tag("manage_invite_search").assertExists()
            assertNoEditLabels()

            tag("manage_class_options").performClick()
            waitForIdle()
            assertTextShown("Cancel whole class")
            assertNoEditLabels()
        }
    }

    @Test
    fun F4_AC17_scheduleShowsTheHeaderTabsCalloutAndTenDates() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            world.setToday(MON_5_OCT_2026)
            openCoreBurn(world, d1)

            assertTextShown("Public")
            assertTextShown("Ends Sat 31 Oct 2026 \u00B7 10 dates")
            assertTextShown("TRX Core Burn")
            assertTextShown("Mon \u00B7 Wed \u00B7 5:30 PM \u2013 6:30 PM")
            assertTextUnder("manage_tab_schedule", "Schedule")
            assertTextUnder("manage_tab_requests", "Requests")
            assertTextUnder("manage_tab_roster", "Roster")
            assertTextUnder("manage_tab_invite", "Invite")
            assertTextUnder("manage_tab_requests_badge", "3")
            tag("manage_tab_schedule").assertIsSelected()
            assertTextShown("Cancel a single date")
            assertTextShown("Only today or a future date can be cancelled. The class keeps repeating.")
            assertTextShown("DATES")
            assertTextShown("10 dates")

            assertEquals(CORE_BURN_DATES, cardIdsTopToBottom("manage_date_row_"))
            val first = "manage_date_row_2026-09-28"
            assertTextUnder(first, "SESSION 1")
            assertTextUnder(first, "Mon 28 Sep 2026")
            assertTextUnder(first, "5:30 PM \u2013 6:30 PM")
            assertTextUnder(first, "Scheduled")
            assertTextUnder(first, "Past date")
            tag("manage_cancel_date_2026-09-28").assertDoesNotExist()
            assertTextUnder("manage_date_row_2026-09-30", "Past date")
            tag("manage_cancel_date_2026-09-30").assertDoesNotExist()

            val today = "manage_date_row_2026-10-05"
            assertTextUnder(today, "SESSION 3")
            assertTextUnder(today, "Mon 5 Oct 2026")
            assertTextUnder(today, "TODAY")
            assertNoTextUnder(today, "Past date")
            assertTextUnder("manage_cancel_date_2026-10-05", "Cancel date")
            for (date in CORE_BURN_DATES.drop(3)) {
                assertTextUnder("manage_cancel_date_$date", "Cancel date")
                assertNoTextUnder("manage_date_row_$date", "TODAY")
            }
            assertEquals(CORE_BURN_DATES.drop(2), cardIdsTopToBottom("manage_cancel_date_"), "today and later dates only")
        }
    }

    @Test
    fun F4_AC18_cancelDateConfirmsKeepsOrCancelsAndShowsATimedToast() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            world.setToday(MON_5_OCT_2026)
            openCoreBurn(world, d1)

            tag("manage_cancel_date_2026-10-05").performClick()
            waitForIdle()
            assertTextUnder("manage_dialog", "Cancel Mon 5 Oct?")
            assertTextUnder(
                "manage_dialog",
                "Session 3 of TRX Core Burn will show as Cancelled and won\u2019t accept attendance. " +
                    "Wed 7 Oct and every other date stay scheduled.", // section 10 Q4: next scheduled date (Session 4)
            )
            assertTextUnder("manage_dialog_keep", "Keep date")
            assertTextUnder("manage_dialog_confirm", "Cancel date")

            tag("manage_dialog_keep").performClick()
            waitForIdle()
            tag("manage_dialog").assertDoesNotExist()
            assertEquals(SessionStatus.SCHEDULED, statusOn(world, d1, MON_5_OCT_2026))
            assertTextUnder("manage_date_row_2026-10-05", "Scheduled")

            tag("manage_cancel_date_2026-10-05").performClick()
            waitForIdle()
            tag("manage_dialog_confirm").performClick()
            waitForIdle()

            tag("manage_dialog").assertDoesNotExist()
            assertEquals(SessionStatus.CANCELLED, statusOn(world, d1, MON_5_OCT_2026))
            assertTextUnder("manage_date_row_2026-10-05", "Cancelled")
            assertEquals(SessionStatus.SCHEDULED, statusOn(world, d1, LocalDate(2026, 10, 7)), "Session 4 stays Scheduled")
            assertEquals(SessionStatus.SCHEDULED, statusOn(world, d1, LocalDate(2026, 10, 12)), "Session 5 stays Scheduled")
            assertTextUnder("manage_date_row_2026-10-07", "Scheduled")
            assertTextUnder("manage_date_row_2026-10-12", "Scheduled")
            assertTextUnder("manage_toast", "Mon 5 Oct cancelled. Other dates stay scheduled.")
            assertNoUndo()

            mainClock.advanceTimeBy(4_100)
            waitForIdle()
            tag("manage_toast").assertDoesNotExist()
        }
    }

    private fun ComposeUiTest.openCoreBurn(world: Flow4Harness, d1: Flow4Example) {
        world.signInAsMara()
        mountFlow4(world)
        openCoachCard(d1.coreBurn.id)
    }

    private fun ComposeUiTest.editableFields(): Int =
        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.EditableText), useUnmergedTree = true)
            .fetchSemanticsNodes(atLeastOneRootRequired = false).size

    /** None of the create form's editing labels, and no "Edit" control. */
    private fun ComposeUiTest.assertNoEditLabels() {
        for (label in listOf("Class name", "Repeats", "Time slots", "Visibility", "Ends", "End date", "Save class")) {
            assertTextNotShown(label)
        }
        assertTextNotShown("Edit", substring = false)
        assertTextNotShown("Edit class", substring = true)
    }

    private fun statusOn(world: Flow4Harness, d1: Flow4Example, date: LocalDate): SessionStatus =
        world.classes.sessionsOf(d1.coreBurn.id).single { it.date == date }.status
}
