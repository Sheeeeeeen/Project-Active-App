package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.presentation.common.UiError
import ph.project.active.presentation.manageclass.InviteConfirmation
import ph.project.active.testkit.Flow3Example
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm

class F3P6InvitesRefreshTest {
    @Test
    fun F3_P6_refreshShowsANewInviteAndKeepsTheConfirmation() = runVm {
        Phase2World().use { world ->
            val example = Flow3Example(world)
            val viewModel = world.invites(example.account)
            viewModel.onAccept(example.mobilityFlow.id)
            val accepted = InviteConfirmation.InviteAccepted("TRX Mobility Flow")
            assertEquals(accepted, viewModel.state.value.confirmation)
            assertEquals(0, viewModel.state.value.pendingInviteCount)

            world.inviteByEmployeeId(example.everyDay20.id, "EMP-20417")
            viewModel.refresh()

            assertEquals(listOf(example.everyDay20.id), viewModel.state.value.invites.map { it.classId })
            assertEquals(1, viewModel.state.value.pendingInviteCount)
            assertFalse(viewModel.state.value.isEmpty)
            assertEquals(accepted, viewModel.state.value.confirmation)
        }
    }

    @Test
    fun F3_P6_refreshClearsTheError() = runVm {
        Phase2World().use { world ->
            val example = Flow3Example(world)
            val viewModel = world.invites(example.account)
            world.network.set(NetworkStatus.OFFLINE)
            viewModel.onAccept(example.mobilityFlow.id)
            assertEquals(UiError.Offline, viewModel.state.value.error)

            world.network.set(NetworkStatus.ONLINE)
            viewModel.refresh()

            assertNull(viewModel.state.value.error)
            assertEquals(listOf(example.mobilityFlow.id), viewModel.state.value.invites.map { it.classId })
        }
    }
}
