package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.presentation.common.UiError
import ph.project.active.presentation.manageclass.InviteConfirmation
import ph.project.active.testkit.Flow3Example
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm

class F3P5DeclineConfirmationTest {
    @Test
    fun F3_P5_declineSetsInviteDeclinedAndHandlingClearsIt() = runVm {
        Phase2World().use { world ->
            val example = Flow3Example(world, invitedToBootcamp = true)
            val viewModel = world.invites(example.account)

            viewModel.onDecline(example.mobilityFlow.id)
            assertEquals(listOf(example.bootcamp.id), viewModel.state.value.invites.map { it.classId })
            assertEquals(InviteConfirmation.InviteDeclined, viewModel.state.value.confirmation)

            viewModel.onConfirmationHandled()
            assertNull(viewModel.state.value.confirmation)

            viewModel.onAccept(example.bootcamp.id)
            assertEquals(
                InviteConfirmation.InviteAccepted("Weekend TRX Bootcamp"),
                viewModel.state.value.confirmation,
            )
        }
    }

    @Test
    fun F3_P5_anOfflineTapClearsAnEarlierConfirmationSoAFailureNeverShowsASuccessToast() = runVm {
        Phase2World().use { world ->
            val example = Flow3Example(world, invitedToBootcamp = true)
            val viewModel = world.invites(example.account)
            viewModel.onAccept(example.mobilityFlow.id)
            assertEquals(
                InviteConfirmation.InviteAccepted("TRX Mobility Flow"),
                viewModel.state.value.confirmation,
            )

            world.network.set(NetworkStatus.OFFLINE)
            viewModel.onDecline(example.bootcamp.id)

            assertEquals(UiError.Offline, viewModel.state.value.error)
            assertNull(viewModel.state.value.confirmation)
            assertEquals(listOf(example.bootcamp.id), viewModel.state.value.invites.map { it.classId })
        }
    }
}
