package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount

class F4P5CancelledClassInManageTest {
    @Test
    fun F4_P5_aCancelledClassOpensWithIsCancelledAndNoCancellableDates() = runVm {
        Phase2World().use { world ->
            world.setLocal(MON_28_SEP_2026, 9)
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id)
            assertFalse(world.manage(coach.toAccount(), trxClass.id).state.value.isCancelled)

            world.cancelClass(trxClass.id)
            val state = world.manage(coach.toAccount(), trxClass.id).state.value

            assertTrue(state.isCancelled)
            assertTrue(state.schedule.isNotEmpty())
            assertTrue(state.schedule.none { it.canCancelDate })
        }
    }
}
