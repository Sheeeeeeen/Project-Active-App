package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.domain.Visibility
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount

class StudentClassesViewModelTest {

    @Test
    fun AC45_my_classes_and_public_classes_use_membership_chips_and_drop_cancelled() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val joined = world.createMonWedEveningClass(coach.id, name = "Joined")
            val invited = world.createMonWedEveningClass(coach.id, name = "Invited")
            val pending = world.createMonWedEveningClass(coach.id, name = "Pending")
            val open = world.createMonWedEveningClass(coach.id, name = "Open")
            val privateClass = world.createMonWedEveningClass(coach.id, visibility = Visibility.PRIVATE, name = "Private")
            val student = world.student()
            world.makeActive(joined.id, student.id)
            world.makeActive(privateClass.id, student.id)
            world.inviteByEmployeeId(invited.id, student.employeeId!!)
            world.requestToJoin(pending.id, student.id)
            world.cancelClass(joined.id)

            val viewModel = world.studentClasses(student.toAccount())
            assertEquals(student.name, viewModel.state.value.studentName)
            assertEquals(student.employeeId, viewModel.state.value.employeeId)
            assertEquals(listOf(privateClass.id), viewModel.state.value.myClasses.map { it.classId })
            assertEquals(ClassChip.Joined, viewModel.state.value.myClasses.single().chip)
            assertTrue(viewModel.state.value.myClasses.none { it.classId == joined.id })

            val public = viewModel.state.value.publicClasses.associateBy { it.classId }
            assertEquals(setOf(invited.id, pending.id, open.id), public.keys)
            assertEquals(ClassChip.Invited, public.getValue(invited.id).chip)
            assertEquals(ClassChip.Pending, public.getValue(pending.id).chip)
            assertNull(public.getValue(open.id).chip)
            assertTrue(viewModel.state.value.publicClasses.none { it.classId == privateClass.id })

            viewModel.onClassTapped(open.id)
            assertEquals(Navigation.OpenClassDetail(open.id), viewModel.state.value.navigation)
        }
    }
}
