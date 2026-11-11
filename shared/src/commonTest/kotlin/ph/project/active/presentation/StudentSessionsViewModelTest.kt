package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import ph.project.active.domain.model.StudentSessionStatus
import ph.project.active.testkit.EVENING
import ph.project.active.testkit.FRI_25_SEP_2026
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.MORNING
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.TUE_29_SEP_2026
import ph.project.active.testkit.WED_30_SEP_2026
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount

class StudentSessionsViewModelTest {

    @Test
    fun AC58_rows_match_student_sessions_and_empty_is_explicit() = runVm {
        Phase2World().use { world ->
            val student = world.student().toAccount()
            assertTrue(world.studentSessionsVm(student).state.value.isEmpty)
            val coach = world.coach()
            world.giveKeys(coach.id)
            val trxClass = world.createMonWedEveningClass(coach.id, endDate = WED_30_SEP_2026)
            world.makeActive(trxClass.id, student.id)
            world.cancelDate(trxClass.id, WED_30_SEP_2026)
            val viewModel = world.studentSessionsVm(student)
            val rows = viewModel.state.value.upcoming + viewModel.state.value.today + viewModel.state.value.earlier
            assertEquals(1, rows.size)
            assertEquals("TRX Evening", rows.single().className)
            assertEquals(1, rows.single().sessionNumber)
            assertEquals(MON_28_SEP_2026, rows.single().date)
            assertEquals(EVENING, rows.single().slot)
            assertEquals(StudentSessionStatus.UPCOMING, rows.single().status)
            assertEquals(false, rows.single().uploadPending)
            assertFalse(viewModel.state.value.isEmpty)
        }
    }

    @Test
    fun AC59_groups_are_latest_first() = runVm {
        Phase2World(start = FRI_25_SEP_2026.atTime(9, 0)).use { world ->
            val coach = world.coach()
            val trxClass = world.createTwoSlotMondayClass(coach.id, endDate = WED_30_SEP_2026)
            val student = world.student()
            world.makeActive(trxClass.id, student.id)
            world.setLocal(MON_28_SEP_2026, 12)
            val state = world.studentSessionsVm(student.toAccount()).state.value
            assertEquals(listOf(WED_30_SEP_2026), state.upcoming.map { it.date })
            assertEquals(listOf(EVENING, MORNING), state.today.map { it.slot })
            assertTrue(state.earlier.isEmpty())
            world.setLocal(TUE_29_SEP_2026, 9)
            val later = world.studentSessionsVm(student.toAccount()).state.value
            assertEquals(listOf(EVENING, MORNING), later.earlier.map { it.slot })
            assertEquals(listOf(MON_28_SEP_2026, MON_28_SEP_2026), later.earlier.map { it.date })
        }
    }

    @Test
    fun AC60_the_day_boundary_follows_the_injected_time_zone() = runVm {
        val instant = TUE_29_SEP_2026.atTime(0, 0).toInstant(TimeZone.of("Asia/Manila"))
        fun status(zone: TimeZone): StudentSessionStatus {
            val world = Phase2World(start = FRI_25_SEP_2026.atTime(9, 0), timeZone = zone)
            val trxClass = world.createMonWedEveningClass(world.coach().id)
            val student = world.student()
            world.makeActive(trxClass.id, student.id)
            world.clock.current = instant
            val row = world.studentSessionsVm(student.toAccount()).state.value.let { state ->
                (state.upcoming + state.today + state.earlier).single { it.date == MON_28_SEP_2026 }
            }
            val status = row.status
            world.close()
            return status
        }
        assertEquals(StudentSessionStatus.ABSENT, status(TimeZone.of("Asia/Manila")))
        assertEquals(StudentSessionStatus.UPCOMING, status(TimeZone.of("America/New_York")))

        Phase2World().use { world ->
            val trxClass = world.createMonWedEveningClass(world.coach().id)
            val student = world.student()
            world.makeActive(trxClass.id, student.id)
            world.setLocal(MON_28_SEP_2026, 23, 59)
            val late = world.studentSessionsVm(student.toAccount())
            assertEquals(StudentSessionStatus.UPCOMING, late.state.value.today.single().status)
            world.setLocal(TUE_29_SEP_2026, 0, 0)
            late.refresh()
            assertEquals(StudentSessionStatus.ABSENT, late.state.value.earlier.single().status)
        }
    }
}
