package ph.project.active.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.TimeSlot
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.OFFLINE_NOTHING_SAVED
import ph.project.active.ui.testing.assertTextNotShown
import ph.project.active.ui.testing.assertTextShown
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.coachRoutes
import ph.project.active.ui.testing.mountFlow4
import ph.project.active.ui.testing.runFlow4UiTest
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.tapTwiceQuickly

private val FRI_25_SEP_2026 = LocalDate(2026, 9, 25)
private val SAT_31_OCT_2026 = LocalDate(2026, 10, 31)
private val EVENING = TimeSlot(LocalTime(17, 30), LocalTime(18, 30))

/**
 * D3-D6. The name is typed into the field. Days, time slots and the end date use the platform
 * pickers (spec section 9, item 12), so the tests set them on the screen's CreateClassViewModel.
 */
@OptIn(ExperimentalTestApi::class)
class F4CreateClassScreenTest {
    @Test
    fun F4_AC11_createOpensWithTheAc62Defaults() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.signInAsMara()
            mountFlow4(world)
            openCreate()

            assertTextUnder("create_class_title", "New class")
            assertTextUnder("create_class_cancel", "Cancel")
            assertTextShown("Class name")
            assertTextShown("e.g. TRX Core Burn")
            assertEquals("", world.lastCreateClass!!.state.value.name)
            onNodeWithText("Specific days").assertIsSelected()
            assertTextShown("Every day")
            assertTextShown("Choose at least one day. Each day gets its own time slots.")
            assertTrue(world.lastCreateClass!!.state.value.selectedDays.isEmpty(), "no day is selected")
            onNodeWithText("Public").assertIsSelected()
            assertTextShown("Private")
            assertTextShown("Public: anyone can see it and request to join.")
            onNode(hasText("No end date") and isToggleable()).assertIsOn()
            assertTextNotShown("End date")
            assertTextUnder("create_class_save", "Save class")
            tag("create_class_save").assertIsNotEnabled()
        }
    }

    @Test
    fun F4_AC11_cancelPopsToD1AndCreatesNothing() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.signInAsMara()
            val nav = mountFlow4(world)
            openCreate()
            tag("create_class_name").performTextInput("TRX Core Burn")
            tag("create_class_cancel").performClick()
            waitForIdle()
            assertEquals(listOf("CoachHome"), coachRoutes(nav))
            assertTrue(world.classes.allClasses().isEmpty())
            tag("coach_classes_empty").assertExists()
        }
    }

    @Test
    fun F4_AC11_openingCreateTwiceQuicklyAddsOneEntry() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.signInAsMara()
            val nav = mountFlow4(world)
            tapTwiceQuickly("coach_classes_add")
            assertEquals(listOf("CoachHome", "CreateClass"), coachRoutes(nav))
        }
    }

    @Test
    fun F4_AC12_theAc65ExamplePreviewsTenDatesAndSaveReturnsToD1() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.setToday(FRI_25_SEP_2026)
            world.signInAsMara()
            val nav = mountFlow4(world)
            openCreate()
            fillCoreBurn(world, endDate = SAT_31_OCT_2026)

            assertTrue(world.lastCreateClass!!.state.value.canSave)
            tag("create_class_save").assertIsEnabled()
            assertTextUnder("create_class_preview", "10 dates will be created")
            assertTextUnder(
                "create_class_preview",
                "Saving today, Fri 25 Sep 2026: every Monday and Wednesday from Mon 28 Sep to Wed 28 Oct 2026. " +
                    "Past dates are not created. The end date is included.",
            )

            tag("create_class_save").performClick()
            waitForIdle()

            assertEquals(listOf("CoachHome"), coachRoutes(nav))
            val saved = world.classes.allClasses().single()
            assertEquals("TRX Core Burn", saved.name)
            assertEquals(SAT_31_OCT_2026, saved.endDate)
            assertTextUnder("coach_class_card_${saved.id}", "TRX Core Burn")
            assertTextUnder("coach_classes_count", "1 class")
        }
    }

    @Test
    fun F4_AC13_noEndDateShowsTheD5CalloutAndHidesThePicker() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.setToday(FRI_25_SEP_2026)
            world.signInAsMara()
            mountFlow4(world)
            openCreate()
            fillCoreBurn(world, endDate = null)

            assertTextUnder("create_class_preview", "No end date")
            assertTextUnder(
                "create_class_preview",
                "This class keeps repeating until you cancel the whole class. Dates are created from today.",
            )
            assertTextNotShown("End date")
            tag("create_class_save").assertIsEnabled()
        }
    }

    @Test
    fun F4_AC14_aSlotThatEndsBeforeItStartsIsNotAdded() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.signInAsMara()
            mountFlow4(world)
            openCreate()
            val viewModel = world.lastCreateClass!!
            runOnIdle {
                viewModel.onToggleDay(DayOfWeek.MONDAY)
                viewModel.onAddSlot(DayOfWeek.MONDAY, LocalTime(18, 30), LocalTime(17, 30))
            }
            waitForIdle()
            assertTextShown("The end time must be after the start time.")
            assertTrue(viewModel.state.value.slotsByWeekday[DayOfWeek.MONDAY].orEmpty().isEmpty())

            runOnIdle { viewModel.onAddSlot(DayOfWeek.MONDAY, LocalTime(17, 30), LocalTime(17, 30)) }
            waitForIdle()
            assertTextShown("The end time must be after the start time.")
            assertTrue(viewModel.state.value.slotsByWeekday[DayOfWeek.MONDAY].orEmpty().isEmpty())
        }
    }

    @Test
    fun F4_AC14_aBlankNameShowsRequiredAndKeepsSaveDisabled() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.signInAsMara()
            mountFlow4(world)
            openCreate()
            fillCoreBurn(world, endDate = null)
            tag("create_class_save").assertIsEnabled()

            tag("create_class_name").performTextReplacement("")
            waitForIdle()
            assertTextShown("Required.")
            tag("create_class_save").assertIsNotEnabled()
        }
    }

    @Test
    fun F4_AC14_anEndDateBeforeTodayCreatesNothingAndKeepsTheForm() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.signInAsMara()
            val nav = mountFlow4(world)
            openCreate()
            fillCoreBurn(world, endDate = LocalDate(2026, 9, 27))

            tag("create_class_save").performClick()
            waitForIdle()

            assertTextUnder("create_class_form_error", "End date cannot be in the past.")
            assertTrue(world.classes.allClasses().isEmpty())
            assertEquals(listOf("CoachHome", "CreateClass"), coachRoutes(nav))
            assertTextUnder("create_class_name", "TRX Core Burn")
            assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), world.lastCreateClass!!.state.value.selectedDays)
        }
    }

    @Test
    fun F4_AC15_offlineSaveKeepsTheFormWritesNothingAndDoesNotRetry() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.setToday(FRI_25_SEP_2026)
            world.signInAsMara()
            val nav = mountFlow4(world)
            openCreate()
            fillCoreBurn(world, endDate = SAT_31_OCT_2026)
            world.network.set(NetworkStatus.OFFLINE)
            waitForIdle()
            tag("offline_banner").assertExists()

            tag("create_class_save").performClick()
            waitForIdle()
            assertTextUnder("create_class_form_error", OFFLINE_NOTHING_SAVED)
            assertTrue(world.classes.allClasses().isEmpty())
            assertEquals(listOf("CoachHome", "CreateClass"), coachRoutes(nav))
            assertTextUnder("create_class_name", "TRX Core Burn")
            val form = world.lastCreateClass!!.state.value
            assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), form.selectedDays)
            assertEquals(SAT_31_OCT_2026, form.endDate)

            world.network.set(NetworkStatus.ONLINE)
            waitForIdle()
            mainClock.advanceTimeBy(10_000)
            waitForIdle()
            assertTrue(world.classes.allClasses().isEmpty(), "nothing retries on its own")

            tag("create_class_save").performClick()
            waitForIdle()
            assertEquals(listOf("CoachHome"), coachRoutes(nav))
            assertEquals(1, world.classes.allClasses().size)
            tag("create_class_form_error").assertDoesNotExist()
        }
    }

    private fun ComposeUiTest.openCreate() {
        tag("coach_classes_add").performClick()
        waitForIdle()
    }

    /** The create example data: "TRX Core Burn", Specific days, Monday and Wednesday, 5:30 PM - 6:30 PM, Public. */
    private fun ComposeUiTest.fillCoreBurn(world: Flow4Harness, endDate: LocalDate?) {
        tag("create_class_name").performTextInput("TRX Core Burn")
        val viewModel = world.lastCreateClass!!
        runOnIdle {
            viewModel.onToggleDay(DayOfWeek.MONDAY)
            viewModel.onToggleDay(DayOfWeek.WEDNESDAY)
            viewModel.onAddSlot(DayOfWeek.MONDAY, EVENING.start, EVENING.end)
            viewModel.onAddSlot(DayOfWeek.WEDNESDAY, EVENING.start, EVENING.end)
            viewModel.onEndDateChange(endDate)
        }
        waitForIdle()
    }
}
