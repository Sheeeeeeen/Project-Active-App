package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.domain.NetworkStatus
import ph.project.active.domain.Visibility
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm

class GuestClassListViewModelTest {

    @Test
    fun AC37_lists_public_classes_that_are_not_cancelled_in_creation_order() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            assertTrue(world.guestClasses().state.value.isEmpty)
            val first = world.createMonWedEveningClass(coach.id, name = "First")
            val second = world.createMonWedEveningClass(coach.id, name = "Second")
            world.createMonWedEveningClass(coach.id, visibility = Visibility.PRIVATE, name = "Hidden")
            val viewModel = world.guestClasses()
            assertEquals(listOf(first.id, second.id), viewModel.state.value.items.map { it.classId })
            assertEquals(listOf("First", "Second"), viewModel.state.value.items.map { it.name })
            assertEquals(listOf("Coach 1", "Coach 1"), viewModel.state.value.items.map { it.coachName })
            assertEquals(first.slotsByWeekday, viewModel.state.value.items.first().slotsByWeekday)
            assertEquals(false, viewModel.state.value.isEmpty)

            world.cancelClass(first.id)
            assertEquals(listOf(second.id), world.guestClasses().state.value.items.map { it.classId })
        }
    }

    @Test
    fun AC38_register_and_class_taps_navigate_and_the_offline_list_comes_from_cache() = runVm {
        Phase2World().use { world ->
            val trxClass = world.createMonWedEveningClass(world.coach().id)
            world.network.set(NetworkStatus.OFFLINE)
            val viewModel = world.guestClasses()
            assertEquals(listOf(trxClass.id), viewModel.state.value.items.map { it.classId })
            assertEquals("No internet connection", viewModel.state.value.connection.banner)
            viewModel.onRegisterOrSignIn()
            assertEquals(Navigation.OpenRegistration(RegistrationOrigin.FromClassList), viewModel.state.value.navigation)
            viewModel.onNavigationHandled()
            assertNull(viewModel.state.value.navigation)
            viewModel.onClassTapped(trxClass.id)
            assertEquals(Navigation.OpenClassDetail(trxClass.id), viewModel.state.value.navigation)
        }
    }
}
