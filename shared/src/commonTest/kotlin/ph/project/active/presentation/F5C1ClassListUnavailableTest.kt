package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.Visibility
import ph.project.active.testkit.EVENING
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.SAT_31_OCT_2026
import ph.project.active.testkit.runVm

/** Flow 5 spec C1: `classListUnavailable` is true only when the coach is offline and has no classes. */
class F5C1ClassListUnavailableTest {

    // C1 / F5-AC9: "online with no classes → classListUnavailable is false, todaysSessions is empty, and code is not blank"
    @Test
    fun F5_C1_onlineWithNoClassesShowsTheCodeAndIsNotUnavailable() = runVm {
        Phase2World().use { world ->
            val coach = world.coachAccount()
            world.setLocal(MON_28_SEP_2026, 9)

            val state = world.dailyQr(coach).state.value

            assertFalse(state.classListUnavailable)
            assertTrue(state.todaysSessions.isEmpty())
            assertTrue(state.code.isNotBlank())
        }
    }

    // C1 / F5-AC8: "offline with no classes → classListUnavailable is true and code is not blank"
    @Test
    fun F5_C1_offlineWithNoClassesIsUnavailableAndStillHasACode() = runVm {
        Phase2World().use { world ->
            val coach = world.coachAccount()
            world.setLocal(MON_28_SEP_2026, 9)
            world.network.set(NetworkStatus.OFFLINE)

            val state = world.dailyQr(coach).state.value

            assertTrue(state.classListUnavailable)
            assertTrue(state.code.isNotBlank())
        }
    }

    // C1 / F5-AC7: "offline after creating a class for this coach → classListUnavailable is false and todaysSessions is filled"
    @Test
    fun F5_C1_offlineWithAClassIsNotUnavailableAndListsTodaysSession() = runVm {
        Phase2World().use { world ->
            val coach = world.coachAccount()
            world.setLocal(MON_28_SEP_2026, 9)
            world.network.set(NetworkStatus.OFFLINE)
            world.createMonWedEveningClass(coach.id)

            val state = world.dailyQr(coach).state.value

            assertFalse(state.classListUnavailable)
            assertEquals(listOf(EVENING), state.todaysSessions.map { it.slot })
        }
    }

    // C1 / F5-AC10: "online with classes but no sessions today … still shows the code … with an empty list and no callout"
    @Test
    fun F5_C1_onlineWithClassesButNoSessionTodayIsNotUnavailable() = runVm {
        Phase2World().use { world ->
            val coach = world.coachAccount()
            world.setLocal(MON_28_SEP_2026, 9)
            world.createClass(
                coachId = coach.id,
                name = "Tuesday only",
                visibility = Visibility.PUBLIC,
                slotsByWeekday = mapOf(DayOfWeek.TUESDAY to listOf(EVENING)),
                endDate = SAT_31_OCT_2026,
            )

            val state = world.dailyQr(coach).state.value

            assertFalse(state.classListUnavailable)
            assertTrue(state.todaysSessions.isEmpty())
            assertTrue(state.code.isNotBlank())
        }
    }

    // C1, decision 10: "classListUnavailable is true only when connection.isOffline and the coach has no classes"
    // (literal reading: it follows the connection while the screen is open, not only when the ViewModel is built)
    @Test
    fun F5_C1_unavailableFollowsTheConnectionWhileTheScreenIsOpen() = runVm {
        Phase2World().use { world ->
            val coach = world.coachAccount()
            world.setLocal(MON_28_SEP_2026, 9)
            val viewModel = world.dailyQr(coach)
            assertFalse(viewModel.state.value.classListUnavailable)

            world.network.set(NetworkStatus.OFFLINE)
            assertTrue(viewModel.state.value.classListUnavailable, "offline with no classes")

            world.network.set(NetworkStatus.ONLINE)
            assertFalse(viewModel.state.value.classListUnavailable, "back online")
        }
    }
}
