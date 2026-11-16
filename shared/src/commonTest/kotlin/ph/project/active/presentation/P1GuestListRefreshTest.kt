package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.SAT_31_OCT_2026

class P1GuestListRefreshTest {
    @Test
    fun P1_refreshReloadsClassesFromTheRepository() {
        Phase2World().use { world ->
            val coach = world.coach()
            val viewModel = guestList(world)
            assertTrue(viewModel.state.value.items.isEmpty())
            val added = world.createMonWedEveningClass(coach.id, name = "TRX Core Burn", endDate = SAT_31_OCT_2026)
            assertTrue(viewModel.state.value.items.none { it.classId == added.id })
            viewModel.refresh()
            assertEquals(listOf(added.id), viewModel.state.value.items.map { it.classId })
        }
    }
}

private fun guestList(world: Phase2World): GuestClassListViewModel = GuestClassListViewModel(
    classes = world.classes,
    people = world.people,
    network = world.network,
    clock = world.clock,
    timeZone = world.timeZone,
)
