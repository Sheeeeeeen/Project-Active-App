package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import ph.project.active.presentation.manageclass.ManageConfirmation
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.WED_30_SEP_2026
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount

class F4P6ConfirmationAndOptionsTest {
    @Test
    fun F4_P6_onConfirmationHandledClearsDateCancelled() = runVm {
        Phase2World().use { world ->
            world.setLocal(MON_28_SEP_2026, 9)
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id)
            val viewModel = world.manage(coach.toAccount(), trxClass.id)

            viewModel.onCancelDate(WED_30_SEP_2026)
            viewModel.onConfirmCancelDate()
            assertEquals(ManageConfirmation.DateCancelled(WED_30_SEP_2026), viewModel.state.value.confirmation)

            viewModel.onConfirmationHandled()
            assertNull(viewModel.state.value.confirmation)
        }
    }

    @Test
    fun F4_P6_closeOptionsAndKeepClassLeaveTheClassUnchanged() = runVm {
        Phase2World().use { world ->
            world.setLocal(MON_28_SEP_2026, 9)
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id)
            val viewModel = world.manage(coach.toAccount(), trxClass.id)

            viewModel.onOpenOptions()
            viewModel.onCancelWholeClass()
            viewModel.onCloseOptions()
            assertFalse(viewModel.state.value.optionsOpen)
            assertFalse(viewModel.state.value.confirmCancelClass)
            assertFalse(world.classes.findClass(trxClass.id)!!.isCancelled)

            viewModel.onOpenOptions()
            viewModel.onCancelWholeClass()
            viewModel.onKeepClass()
            assertFalse(viewModel.state.value.confirmCancelClass)
            assertFalse(world.classes.findClass(trxClass.id)!!.isCancelled)
            assertEquals(trxClass, world.classes.findClass(trxClass.id))
        }
    }
}
