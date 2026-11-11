package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import ph.project.active.domain.DailyCodeCheck
import ph.project.active.domain.NetworkStatus
import ph.project.active.domain.SessionStatus
import ph.project.active.testkit.EVENING
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.MORNING
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.TUE_29_SEP_2026
import ph.project.active.testkit.runVm

class DailyQrViewModelTest {

    @Test
    fun AC75_one_code_covers_the_coach_and_verifies_today() = runVm {
        Phase2World().use { world ->
            val coach = world.coachAccount()
            world.giveKeys(coach.id)
            world.setLocal(MON_28_SEP_2026, 9)
            val viewModel = world.dailyQr(coach)
            assertEquals(MON_28_SEP_2026, viewModel.state.value.today)
            assertEquals("Coach 1", viewModel.state.value.coachName)
            assertEquals(MON_28_SEP_2026, viewModel.state.value.validFor)
            assertEquals(world.generateDailyCode(coach.id, MON_28_SEP_2026), viewModel.state.value.code)
            assertEquals(DailyCodeCheck.VALID, world.verifyDailyCode.check(viewModel.state.value.code, coach.id))
        }
    }

    @Test
    fun AC76_a_missing_private_key_is_generated_once_and_reused() = runVm {
        Phase2World().use { world ->
            val coach = world.coachAccount()
            world.setLocal(MON_28_SEP_2026, 9)
            val first = world.dailyQr(coach)
            val second = world.dailyQr(coach)
            assertEquals(first.state.value.code, second.state.value.code)
            assertTrue(world.secrets.privateKey(coach.id)!!.isNotEmpty())
            assertEquals(DailyCodeCheck.VALID, world.verifyDailyCode.check(first.state.value.code, coach.id))
        }
    }

    @Test
    fun AC77_todays_sessions_include_cancelled_ones_in_start_time_order() = runVm {
        Phase2World().use { world ->
            val coach = world.coachAccount()
            world.giveKeys(coach.id)
            val morning = world.createClass(
                coachId = coach.id,
                name = "Morning",
                visibility = ph.project.active.domain.Visibility.PUBLIC,
                slotsByWeekday = mapOf(kotlinx.datetime.DayOfWeek.MONDAY to listOf(MORNING)),
                endDate = ph.project.active.testkit.SAT_31_OCT_2026,
            )
            val evening = world.createMonWedEveningClass(coach.id, name = "Evening")
            world.setLocal(MON_28_SEP_2026, 8)
            world.cancelDate(morning.id, MON_28_SEP_2026)
            val rows = world.dailyQr(coach).state.value.todaysSessions
            assertEquals(listOf(MORNING, EVENING), rows.map { it.slot })
            assertEquals(listOf("Morning", "Evening"), rows.map { it.className })
            assertEquals(SessionStatus.CANCELLED, rows.first().status)
            assertEquals(SessionStatus.SCHEDULED, rows.last().status)
            assertEquals(evening.id, world.classes.findClass(evening.id)!!.id)
        }
    }

    @Test
    fun AC78_offline_code_still_works_and_refresh_rolls_the_date() = runVm {
        Phase2World().use { world ->
            val coach = world.coachAccount()
            world.setLocal(MON_28_SEP_2026, 9)
            world.network.set(NetworkStatus.OFFLINE)
            val empty = world.dailyQr(coach)
            assertTrue(empty.state.value.classListUnavailable)
            assertTrue(empty.state.value.code.isNotBlank())
            assertEquals("No internet connection", empty.state.value.connection.banner)

            world.createMonWedEveningClass(coach.id)
            val withClass = world.dailyQr(coach)
            assertFalse(withClass.state.value.classListUnavailable)
            assertEquals(1, withClass.state.value.todaysSessions.size)
            val oldCode = withClass.state.value.code
            world.setLocal(TUE_29_SEP_2026, 0, 5)
            withClass.refresh()
            assertEquals(TUE_29_SEP_2026, withClass.state.value.today)
            assertNotEquals(oldCode, withClass.state.value.code)
            assertEquals(DailyCodeCheck.WRONG_DATE, world.verifyDailyCode.check(oldCode, coach.id))
        }
    }
}
