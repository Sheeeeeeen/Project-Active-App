package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.testkit.idle
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ph.project.active.domain.Visibility
import ph.project.active.testkit.EVENING
import ph.project.active.testkit.MORNING
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.SAT_31_OCT_2026
import ph.project.active.testkit.THU_31_DEC_2026
import ph.project.active.testkit.runVm

class CreateClassViewModelTest {

    @Test
    fun AC62_starts_public_with_no_end_date_and_day_actions_lock_and_drop_slots() = runVm {
        Phase2World().use { world ->
            val viewModel = world.createClassVm(world.coachAccount())
            assertEquals("", viewModel.state.value.name)
            assertEquals(RepeatMode.SPECIFIC_DAYS, viewModel.state.value.repeat)
            assertTrue(viewModel.state.value.selectedDays.isEmpty())
            assertEquals(Visibility.PUBLIC, viewModel.state.value.visibility)
            assertNull(viewModel.state.value.endDate)
            assertFalse(viewModel.state.value.daysLocked)

            viewModel.onToggleDay(DayOfWeek.MONDAY)
            viewModel.onAddSlot(DayOfWeek.MONDAY, LocalTime(17, 30), LocalTime(18, 30))
            viewModel.onToggleDay(DayOfWeek.MONDAY)
            assertTrue(viewModel.state.value.slotsByWeekday[DayOfWeek.MONDAY].orEmpty().isEmpty())

            viewModel.onEveryDay()
            assertEquals(DayOfWeek.entries.toSet(), viewModel.state.value.selectedDays)
            assertTrue(viewModel.state.value.daysLocked)
            assertEquals(RepeatMode.EVERY_DAY, viewModel.state.value.repeat)
            viewModel.onToggleDay(DayOfWeek.MONDAY)
            assertTrue(DayOfWeek.MONDAY in viewModel.state.value.selectedDays)
            viewModel.onSpecificDays()
            assertFalse(viewModel.state.value.daysLocked)
            assertEquals(RepeatMode.SPECIFIC_DAYS, viewModel.state.value.repeat)
            viewModel.onToggleDay(DayOfWeek.MONDAY)
            assertFalse(DayOfWeek.MONDAY in viewModel.state.value.selectedDays)
        }
    }

    @Test
    fun AC63_slots_stay_sorted_and_an_invalid_range_is_rejected() = runVm {
        Phase2World().use { world ->
            val viewModel = world.createClassVm(world.coachAccount())
            viewModel.onToggleDay(DayOfWeek.MONDAY)
            viewModel.onAddSlot(DayOfWeek.MONDAY, LocalTime(17, 30), LocalTime(18, 30))
            viewModel.onAddSlot(DayOfWeek.MONDAY, LocalTime(7, 0), LocalTime(8, 0))
            assertEquals(listOf(MORNING, EVENING), viewModel.state.value.slotsByWeekday.getValue(DayOfWeek.MONDAY))
            viewModel.onAddSlot(DayOfWeek.MONDAY, LocalTime(18, 0), LocalTime(17, 0))
            assertEquals(SlotError.InvalidSlot, viewModel.state.value.slotError)
            assertEquals(listOf(MORNING, EVENING), viewModel.state.value.slotsByWeekday.getValue(DayOfWeek.MONDAY))
            viewModel.onRemoveSlot(DayOfWeek.MONDAY, MORNING)
            assertEquals(listOf(EVENING), viewModel.state.value.slotsByWeekday.getValue(DayOfWeek.MONDAY))
        }
    }

    @Test
    fun AC64_can_save_requires_a_name_a_day_and_a_slot_on_every_selected_day() = runVm {
        Phase2World().use { world ->
            val viewModel = world.createClassVm(world.coachAccount())
            assertFalse(viewModel.state.value.canSave)
            viewModel.onNameChange("   ")
            viewModel.onToggleDay(DayOfWeek.MONDAY)
            assertFalse(viewModel.state.value.canSave)
            viewModel.onNameChange("  TRX  ")
            viewModel.onAddSlot(DayOfWeek.MONDAY, LocalTime(17, 30), LocalTime(18, 30))
            assertTrue(viewModel.state.value.canSave)
            viewModel.onToggleDay(DayOfWeek.WEDNESDAY)
            assertFalse(viewModel.state.value.canSave)
            viewModel.onAddSlot(DayOfWeek.WEDNESDAY, LocalTime(17, 30), LocalTime(18, 30))
            assertTrue(viewModel.state.value.canSave)
        }
    }

    @Test
    fun AC65_preview_counts_distinct_dates_and_is_null_without_an_end_date() = runVm {
        Phase2World().use { world ->
            val viewModel = world.createClassVm(world.coachAccount())
            viewModel.onToggleDay(DayOfWeek.MONDAY)
            viewModel.onToggleDay(DayOfWeek.WEDNESDAY)
            viewModel.onAddSlot(DayOfWeek.MONDAY, LocalTime(17, 30), LocalTime(18, 30))
            viewModel.onAddSlot(DayOfWeek.MONDAY, LocalTime(7, 0), LocalTime(8, 0))
            viewModel.onAddSlot(DayOfWeek.WEDNESDAY, LocalTime(17, 30), LocalTime(18, 30))
            assertNull(viewModel.state.value.previewDateCount)
            viewModel.onEndDateChange(SAT_31_OCT_2026)
            assertEquals(10, viewModel.state.value.previewDateCount)

            viewModel.onEveryDay()
            viewModel.onSpecificDays()
            DayOfWeek.entries.forEach { day ->
                if (viewModel.state.value.slotsByWeekday[day].orEmpty().isEmpty()) {
                    viewModel.onAddSlot(day, LocalTime(17, 30), LocalTime(18, 30))
                }
            }
            viewModel.onEndDateChange(THU_31_DEC_2026)
            assertEquals(98, viewModel.state.value.previewDateCount)
            viewModel.onEndDateChange(null)
            assertNull(viewModel.state.value.previewDateCount)
        }
    }

    @Test
    fun AC66_save_persists_selected_days_and_a_past_end_date_or_cancel_creates_nothing() = runVm {
        Phase2World().use { world ->
            val coach = world.coachAccount()
            val viewModel = world.createClassVm(coach)
            viewModel.onNameChange("  TRX Evening  ")
            viewModel.onToggleDay(DayOfWeek.MONDAY)
            viewModel.onToggleDay(DayOfWeek.WEDNESDAY)
            viewModel.onToggleDay(DayOfWeek.FRIDAY)
            viewModel.onAddSlot(DayOfWeek.MONDAY, LocalTime(17, 30), LocalTime(18, 30))
            viewModel.onAddSlot(DayOfWeek.WEDNESDAY, LocalTime(17, 30), LocalTime(18, 30))
            viewModel.onAddSlot(DayOfWeek.FRIDAY, LocalTime(17, 30), LocalTime(18, 30))
            viewModel.onToggleDay(DayOfWeek.FRIDAY)
            viewModel.onVisibilityChange(Visibility.PRIVATE)
            viewModel.onEndDateChange(SAT_31_OCT_2026)
            viewModel.onSave()
            idle()
            val saved = world.classes.findClass(viewModel.state.value.savedClassId!!)!!
            assertEquals("TRX Evening", saved.name)
            assertEquals(Visibility.PRIVATE, saved.visibility)
            assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), saved.slotsByWeekday.keys)
            assertEquals(10, world.sessionsOf(saved.id).map { it.date }.distinct().size)
            assertEquals(listOf(saved.id), world.coachClasses(coach).state.value.classes.map { it.classId })

            val rejected = world.createClassVm(coach)
            rejected.onNameChange("Too early")
            rejected.onToggleDay(DayOfWeek.MONDAY)
            rejected.onAddSlot(DayOfWeek.MONDAY, LocalTime(17, 30), LocalTime(18, 30))
            rejected.onEndDateChange(LocalDate(2026, 9, 24))
            rejected.onSave()
            idle()
            assertEquals(UiError.EndDateInPast, rejected.state.value.error)
            assertNull(rejected.state.value.savedClassId)
            assertEquals(listOf(saved.id), world.classes.allClasses().map { it.id })

            val cancelled = world.createClassVm(coach)
            cancelled.onNameChange("Nope")
            cancelled.onToggleDay(DayOfWeek.MONDAY)
            cancelled.onAddSlot(DayOfWeek.MONDAY, LocalTime(17, 30), LocalTime(18, 30))
            cancelled.onCancel()
            assertNull(cancelled.state.value.savedClassId)
            assertEquals(listOf(saved.id), world.classes.allClasses().map { it.id })
        }
    }
}
