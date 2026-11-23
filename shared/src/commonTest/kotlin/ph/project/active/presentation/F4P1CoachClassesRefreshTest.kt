package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount

class F4P1CoachClassesRefreshTest {
    @Test
    fun F4_P1_refreshLoadsTheCoachsNewClassButNotAnotherCoachsClass() = runVm {
        Phase2World().use { world ->
            world.setLocal(MON_28_SEP_2026, 9)
            val mara = world.coach(1)
            val leo = world.coach(2)
            val viewModel = world.coachClasses(mara.toAccount())
            assertTrue(viewModel.state.value.isEmpty)

            val mine = world.createMonWedEveningClass(mara.id, name = "TRX Core Burn")
            val theirs = world.createMonWedEveningClass(leo.id, name = "TRX Mobility Flow")
            viewModel.refresh()

            val ids = viewModel.state.value.classes.map { it.classId }
            assertEquals(listOf(mine.id), ids)
            assertFalse(theirs.id in ids)
            assertFalse(viewModel.state.value.isEmpty)
        }
    }

    @Test
    fun F4_P1_refreshKeepsTheCurrentConnection() = runVm {
        Phase2World().use { world ->
            val mara = world.coach(1)
            val viewModel = world.coachClasses(mara.toAccount())
            world.network.set(NetworkStatus.OFFLINE)
            world.createMonWedEveningClass(mara.id)
            viewModel.refresh()
            assertTrue(viewModel.state.value.connection.isOffline)
            assertEquals(1, viewModel.state.value.classes.size)
        }
    }
}
