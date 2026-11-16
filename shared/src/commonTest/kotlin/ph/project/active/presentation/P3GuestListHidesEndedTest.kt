package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.testkit.Phase2World

class P3GuestListHidesEndedTest {
    @Test
    fun P3_listHidesAClassThatEndedYesterdayAndKeepsOneEndingToday() {
        Phase2World().use { world ->
            val coach = world.coach()
            world.setLocal(LocalDate(2026, 10, 31), 9)
            val endedYesterday = world.createMonWedEveningClass(
                coach.id,
                endDate = LocalDate(2026, 10, 31),
                name = "Ended Yesterday",
            )
            val endingToday = world.createMonWedEveningClass(
                coach.id,
                endDate = LocalDate(2026, 11, 1),
                name = "Ending Today",
            )
            world.setLocal(LocalDate(2026, 11, 1), 9)
            val listed = GuestClassListViewModel(
                classes = world.classes,
                people = world.people,
                network = world.network,
                clock = world.clock,
                timeZone = world.timeZone,
            ).state.value.items.map { it.classId }
            assertFalse(endedYesterday.id in listed)
            assertTrue(endingToday.id in listed)
        }
    }
}
