package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.StudentSessionStatus
import ph.project.active.presentation.studentsessions.StudentSessionUiRow
import ph.project.active.testkit.Flow7Example
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.TUE_29_SEP_2026
import ph.project.active.testkit.WED_30_SEP_2026
import ph.project.active.testkit.runVm

/** Flow 7 spec S10 and S11 (O1 accepted: Upcoming = next 7 days, soonest first). F7-AC12. */
class F7StudentSessionsOrderTest {

    // F7-AC12: "today = Mobility Flow, Core Burn, Sunrise (non-cancelled, start time descending), then Lunch Express (CANCELLED) last."
    @Test
    fun F7_AC12_todayIsNewestFirstThenTheCancelledRow() = runVm {
        Phase2World().use { world ->
            val example = Flow7Example(world)

            val today = world.studentSessionsVm(example.account).state.value.today

            assertEquals(
                listOf(
                    "TRX Mobility Flow" to StudentSessionStatus.UPCOMING,
                    "TRX Core Burn" to StudentSessionStatus.UPCOMING,
                    "Sunrise TRX Strength" to StudentSessionStatus.PRESENT,
                    "Lunch Express TRX" to StudentSessionStatus.CANCELLED,
                ),
                today.map { it.className to it.status },
            )
            assertEquals(1, today.last().sessionNumber, "the cancelled row keeps its number")
        }
    }

    // F7-AC12: "Every row has sessionId set."
    @Test
    fun F7_AC12_everyRowCarriesItsSessionId() = runVm {
        Phase2World().use { world ->
            val example = Flow7Example(world)
            val state = world.studentSessionsVm(example.account).state.value
            val rows = state.upcoming + state.today + state.earlier
            assertTrue(rows.isNotEmpty())

            for (row in rows) {
                val session = world.classes.allClasses()
                    .flatMap { world.classes.sessionsOf(it.id) }
                    .single { it.id == row.sessionId }
                assertEquals(row.date, session.date, "sessionId of $row")
                assertEquals(row.slot, session.slot, "sessionId of $row")
                assertEquals(row.sessionNumber, session.sessionNumber, "sessionId of $row")
            }
        }
    }

    // F7-AC12: "upcoming = Tue 29 Lunch 2; Wed 30 Sunrise 2, Core Burn 2; Thu 1 Oct Lunch 3, Mobility 2; Fri 2 Oct Sunrise 3; Mon 5 Oct Sunrise 4, Lunch 4, Core Burn 3, Mobility 3. That is date ascending, then start time ascending, and nothing after Mon 5 Oct."
    @Test
    fun F7_AC12_upcomingIsTheNextSevenDaysSoonestFirst() = runVm {
        Phase2World().use { world ->
            val example = Flow7Example(world)

            val upcoming = world.studentSessionsVm(example.account).state.value.upcoming

            assertEquals(EXPECTED_UPCOMING, upcoming.map { it.line() })
            assertTrue(world.studentSessionsVm(example.account).state.value.earlier.isEmpty(), "nothing before Mon 28")
        }
    }

    // F7-AC12: "Cancelling Lunch Express Thu 1 Oct moves nothing: that row stays in place with CANCELLED."
    @Test
    fun F7_AC12_cancellingAnUpcomingDateKeepsItsPlace() = runVm {
        Phase2World().use { world ->
            val example = Flow7Example(world)
            val viewModel = world.studentSessionsVm(example.account)
            world.cancelDate(example.lunchExpress.id, OCT_1)

            viewModel.refresh()

            val upcoming = viewModel.state.value.upcoming
            assertEquals(EXPECTED_UPCOMING, upcoming.map { it.line() })
            val lunch = upcoming.single { it.date == OCT_1 && it.className == "Lunch Express TRX" }
            assertEquals(StudentSessionStatus.CANCELLED, lunch.status)
            assertEquals(3, lunch.sessionNumber)
        }
    }

    private fun StudentSessionUiRow.line(): Triple<LocalDate, String, Int> = Triple(date, className, sessionNumber)
}

private val OCT_1 = LocalDate(2026, 10, 1)
private val OCT_2 = LocalDate(2026, 10, 2)
private val OCT_5 = LocalDate(2026, 10, 5)

/** F7-AC12's Upcoming list on the default clock: (date, class, session number). */
private val EXPECTED_UPCOMING = listOf(
    Triple(TUE_29_SEP_2026, "Lunch Express TRX", 2),
    Triple(WED_30_SEP_2026, "Sunrise TRX Strength", 2),
    Triple(WED_30_SEP_2026, "TRX Core Burn", 2),
    Triple(OCT_1, "Lunch Express TRX", 3),
    Triple(OCT_1, "TRX Mobility Flow", 2),
    Triple(OCT_2, "Sunrise TRX Strength", 3),
    Triple(OCT_5, "Sunrise TRX Strength", 4),
    Triple(OCT_5, "Lunch Express TRX", 4),
    Triple(OCT_5, "TRX Core Burn", 3),
    Triple(OCT_5, "TRX Mobility Flow", 3),
)
