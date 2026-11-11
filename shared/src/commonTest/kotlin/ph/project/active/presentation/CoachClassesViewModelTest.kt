package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.project.active.domain.model.Visibility
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm
import ph.project.active.presentation.common.Navigation

class CoachClassesViewModelTest {

    @Test
    fun AC61_lists_the_coachs_classes_in_creation_order_including_cancelled() = runVm {
        Phase2World().use { world ->
            val coach = world.coachAccount()
            assertTrue(world.coachClasses(coach).state.value.isEmpty)
            val first = world.createMonWedEveningClass(coach.id, name = "First")
            val second = world.createMonWedEveningClass(coach.id, visibility = Visibility.PRIVATE, name = "Second")
            world.createMonWedEveningClass(world.coach(2).id, name = "Other")
            val student = world.student()
            world.requestToJoin(first.id, student.id)
            world.makeActive(second.id, student.id)
            world.cancelClass(first.id)

            val viewModel = world.coachClasses(coach)
            assertEquals(listOf(first.id, second.id), viewModel.state.value.classes.map { it.classId })
            assertEquals(listOf(Visibility.PUBLIC, Visibility.PRIVATE), viewModel.state.value.classes.map { it.visibility })
            assertEquals(listOf(true, false), viewModel.state.value.classes.map { it.isCancelled })
            assertEquals(0, viewModel.state.value.classes[0].rosterCount)
            assertEquals(1, viewModel.state.value.classes[0].pendingRequestCount)
            assertEquals(1, viewModel.state.value.classes[1].rosterCount)
            assertEquals(first.slotsByWeekday, viewModel.state.value.classes[0].slotsByWeekday)
            viewModel.onCreateClass()
            assertEquals(Navigation.OpenCreateClass, viewModel.state.value.navigation)
            viewModel.onNavigationHandled()
            viewModel.onClassTapped(second.id)
            assertEquals(Navigation.OpenManage(second.id), viewModel.state.value.navigation)
        }
    }
}
