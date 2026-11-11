package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.domain.model.CoachAttendanceStatus
import ph.project.active.domain.model.SessionStatus
import ph.project.active.domain.model.Visibility
import ph.project.active.testkit.EVENING
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.MORNING
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.SAT_31_OCT_2026
import ph.project.active.testkit.TUE_29_SEP_2026
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount
import kotlinx.datetime.DayOfWeek
import ph.project.active.presentation.createclass.DayState

class CoachAttendanceViewModelTest {

    @Test
    fun AC79_sections_include_cancelled_sessions_and_the_date_cannot_pass_today() = runVm {
        Phase2World().use { world ->
            val coach = world.coachAccount()
            world.giveKeys(coach.id)
            world.createClass(
                coachId = coach.id,
                name = "Morning",
                visibility = Visibility.PUBLIC,
                slotsByWeekday = mapOf(DayOfWeek.MONDAY to listOf(MORNING)),
                endDate = SAT_31_OCT_2026,
            )
            val evening = world.createMonWedEveningClass(coach.id, name = "Evening")
            world.setLocal(MON_28_SEP_2026, 12)
            world.cancelDate(evening.id, MON_28_SEP_2026)
            val viewModel = world.coachAttendance(coach)
            assertEquals(MON_28_SEP_2026, viewModel.state.value.date)
            assertTrue(!viewModel.state.value.canGoNext)
            viewModel.onNextDay()
            assertEquals(MON_28_SEP_2026, viewModel.state.value.date)
            assertEquals(listOf(MORNING, EVENING), viewModel.state.value.sections.map { it.slot })
            assertEquals(listOf(false, true), viewModel.state.value.sections.map { it.cancelled })
            assertTrue(viewModel.state.value.sections.single { it.cancelled }.students.isEmpty())
            viewModel.onPreviousDay()
            assertEquals(kotlinx.datetime.LocalDate(2026, 9, 27), viewModel.state.value.date)
            assertTrue(viewModel.state.value.canGoNext)
            viewModel.onNextDay()
            assertEquals(MON_28_SEP_2026, viewModel.state.value.date)
        }
    }

    @Test
    fun AC80_rows_show_active_students_and_present_scan_details() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            world.giveKeys(coach.id)
            val trxClass = world.createTwoSlotMondayClass(coach.id)
            val first = world.student(1)
            val second = world.student(2)
            val pending = world.student(3)
            world.makeActive(trxClass.id, first.id)
            world.makeActive(trxClass.id, second.id)
            world.requestToJoin(trxClass.id, pending.id)
            val morning = world.sessionOn(trxClass.id, MON_28_SEP_2026, MORNING)
            world.setLocal(MON_28_SEP_2026, 7, 10)
            world.recordAttendance(first.id, world.refOf(morning), world.todaysCode(coach.id))
            val scannedAt = world.attendance.all().single().scannedAt
            val section = world.coachAttendance(coach.toAccount()).state.value.sections.single { it.slot == MORNING }
            assertEquals(listOf(first.id, second.id), section.students.map { it.studentId })
            val present = section.students.single { it.studentId == first.id }
            assertEquals(CoachAttendanceStatus.PRESENT, present.status)
            assertEquals("Student 1", present.name)
            assertEquals("EMP-1", present.employeeId)
            assertEquals("TRX Double Monday", present.chosenClassName)
            assertEquals(scannedAt, present.scannedAt)
            val waiting = section.students.single { it.studentId == second.id }
            assertEquals(CoachAttendanceStatus.NOT_SCANNED, waiting.status)
            assertNull(waiting.chosenClassName)
            assertNull(waiting.scannedAt)
        }
    }

    @Test
    fun AC81_day_state_counts_present_pairs_and_absent_only_after_the_day() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            world.giveKeys(coach.id)
            val trxClass = world.createTwoSlotMondayClass(coach.id)
            val first = world.student(1)
            val second = world.student(2)
            world.makeActive(trxClass.id, first.id)
            world.makeActive(trxClass.id, second.id)
            world.setLocal(MON_28_SEP_2026, 7, 10)
            val morning = world.sessionOn(trxClass.id, MON_28_SEP_2026, MORNING)
            world.recordAttendance(first.id, world.refOf(morning), world.todaysCode(coach.id))
            val today = world.coachAttendance(coach.toAccount())
            assertEquals(DayState.IN_PROGRESS, today.state.value.dayState)
            assertEquals(1, today.state.value.presentCount)
            assertEquals(0, today.state.value.absentCount)

            world.setLocal(TUE_29_SEP_2026, 0, 0)
            val ended = world.coachAttendance(coach.toAccount())
            ended.onPreviousDay()
            assertEquals(MON_28_SEP_2026, ended.state.value.date)
            assertEquals(DayState.DAY_ENDED, ended.state.value.dayState)
            assertEquals(1, ended.state.value.presentCount)
            assertEquals(3, ended.state.value.absentCount)
            assertEquals(SessionStatus.SCHEDULED, morning.status)
        }
    }
}
