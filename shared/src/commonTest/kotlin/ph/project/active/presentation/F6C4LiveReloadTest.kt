package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.CoachAttendanceStatus
import ph.project.active.presentation.createclass.DayState
import ph.project.active.testkit.Flow6Example
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.TUE_29_SEP_2026
import ph.project.active.testkit.idle
import ph.project.active.testkit.runVm

/** Flow 6 spec C4 (decisions 10 and 11): live reload on store changes, and `refresh()`. */
class F6C4LiveReloadTest {

    private val sun27Sep2026 = LocalDate(2026, 9, 27)

    // C4: "adding a record for Dana on Core Burn makes her row Present and presentCount goes up by one, with no other call"
    @Test
    fun F6_C4_aNewRecordInTheStoreShowsWithNoOtherCall() = runVm {
        Phase2World().use { world ->
            val d22 = Flow6Example(world)
            val viewModel = world.coachAttendance(d22.account)
            assertEquals(3, viewModel.state.value.presentCount)

            d22.scan(d22.dana, d22.coreBurn, MON_28_SEP_2026, 17, 33)
            idle()

            val coreBurn = viewModel.state.value.sections.single { it.className == "TRX Core Burn" }
            assertEquals(CoachAttendanceStatus.PRESENT, coreBurn.students.single { it.studentId == d22.dana.id }.status)
            assertEquals(4, viewModel.state.value.presentCount)
        }
    }

    // C4: "Moving the clock to Tue 29 Sep and calling refresh() shows Tue 29 with dayState = IN_PROGRESS"
    @Test
    fun F6_C4_refreshOnANewDayMovesFromTheOldTodayToTheNewToday() = runVm {
        Phase2World().use { world ->
            val d22 = Flow6Example(world)
            val viewModel = world.coachAttendance(d22.account)
            assertEquals(MON_28_SEP_2026, viewModel.state.value.date)

            world.setLocal(TUE_29_SEP_2026, 9, 41)
            viewModel.refresh()

            assertEquals(TUE_29_SEP_2026, viewModel.state.value.date)
            assertEquals(DayState.IN_PROGRESS, viewModel.state.value.dayState)
            assertEquals(false, viewModel.state.value.canGoNext)
        }
    }

    // C4: "Paging back to Sun 27 Sep, moving the clock a day, and calling refresh() keeps Sun 27"
    @Test
    fun F6_C4_refreshKeepsAnEarlierDateTheCoachPagedBackTo() = runVm {
        Phase2World().use { world ->
            val d22 = Flow6Example(world)
            val viewModel = world.coachAttendance(d22.account)
            viewModel.onPreviousDay()
            assertEquals(sun27Sep2026, viewModel.state.value.date)

            world.setLocal(TUE_29_SEP_2026, 9, 41)
            viewModel.refresh()

            assertEquals(sun27Sep2026, viewModel.state.value.date)
            assertEquals(DayState.DAY_ENDED, viewModel.state.value.dayState)
        }
    }

    // Decision 11 / C4: "otherwise reload the shown date" — refresh() picks up a roster change on the same day
    // (assumption 6: roster changes are not live; they show on the next tab show or foreground)
    @Test
    fun F6_C4_refreshOnTheSameDayReloadsTheShownDate() = runVm {
        Phase2World().use { world ->
            val d22 = Flow6Example(world)
            val viewModel = world.coachAttendance(d22.account)
            world.makeActive(d22.coreBurn.id, d22.hana.id)

            viewModel.refresh()

            assertEquals(MON_28_SEP_2026, viewModel.state.value.date)
            val coreBurn = viewModel.state.value.sections.single { it.className == "TRX Core Burn" }
            assertEquals(
                listOf(d22.paolo.id, d22.dana.id, d22.gia.id, d22.hana.id),
                coreBurn.students.map { it.studentId },
                "Hana joined last, so she is last",
            )
        }
    }

    // Decision 11: "that date stays, and only its data reloads" — a paged-back date picks up a late record on refresh()
    @Test
    fun F6_C4_aPagedBackDateReloadsItsDataOnRefresh() = runVm {
        Phase2World().use { world ->
            val d22 = Flow6Example(world)
            world.setLocal(TUE_29_SEP_2026, 9, 41)
            val viewModel = world.coachAttendance(d22.account)
            viewModel.onPreviousDay()
            assertEquals(3, viewModel.state.value.presentCount)
            assertEquals(3, viewModel.state.value.absentCount)

            d22.scan(d22.gia, d22.coreBurn, MON_28_SEP_2026, 17, 45)
            viewModel.refresh()

            assertEquals(MON_28_SEP_2026, viewModel.state.value.date)
            assertEquals(4, viewModel.state.value.presentCount)
            assertEquals(2, viewModel.state.value.absentCount)
        }
    }
}
