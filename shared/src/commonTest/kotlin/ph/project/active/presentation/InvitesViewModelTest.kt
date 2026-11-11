package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.domain.MembershipStatus
import ph.project.active.domain.Visibility
import ph.project.active.domain.activeRoster
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount

class InvitesViewModelTest {

    @Test
    fun AC46_lists_current_invites_and_an_empty_state() = runVm {
        Phase2World().use { world ->
            val student = world.student().toAccount()
            assertTrue(world.invites(student).state.value.isEmpty)
            assertEquals(0, world.invites(student).state.value.pendingInviteCount)
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id, visibility = Visibility.PRIVATE, name = "Private")
            world.inviteByEmployeeId(trxClass.id, student.employeeId!!)
            val viewModel = world.invites(student)
            assertEquals(1, viewModel.state.value.pendingInviteCount)
            assertEquals(false, viewModel.state.value.isEmpty)
            val row = viewModel.state.value.invites.single()
            assertEquals(trxClass.id, row.classId)
            assertEquals("Private", row.className)
            assertEquals("Coach 1", row.coachName)
            assertEquals(Visibility.PRIVATE, row.visibility)
            assertEquals(trxClass.slotsByWeekday, row.slotsByWeekday)
        }
    }

    @Test
    fun AC47_accept_and_decline_follow_phase1_and_a_cancelled_class_invite_stays_listed() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val accepted = world.createMonWedEveningClass(coach.id, name = "Accepted")
            val declined = world.createMonWedEveningClass(coach.id, name = "Declined")
            val cancelled = world.createMonWedEveningClass(coach.id, name = "Cancelled")
            val student = world.student()
            val employeeId = student.employeeId!!
            world.inviteByEmployeeId(accepted.id, employeeId)
            world.inviteByEmployeeId(declined.id, employeeId)
            world.inviteByEmployeeId(cancelled.id, employeeId)
            world.cancelClass(cancelled.id)
            val account = student.toAccount()
            val viewModel = world.invites(account)
            assertEquals(setOf(accepted.id, declined.id, cancelled.id), viewModel.state.value.invites.map { it.classId }.toSet())

            viewModel.onAccept(accepted.id)
            assertEquals(MembershipStatus.ACTIVE, world.statusOf(accepted.id, student.id))
            assertTrue(viewModel.state.value.invites.none { it.classId == accepted.id })
            assertEquals(InviteConfirmation.InviteAccepted("Accepted"), viewModel.state.value.confirmation)
            assertEquals(listOf(accepted.id), world.studentClasses(account).state.value.myClasses.map { it.classId })

            viewModel.onDecline(declined.id)
            assertEquals(MembershipStatus.DECLINED, world.statusOf(declined.id, student.id))
            assertTrue(world.memberships.activeRoster(declined.id).none { it == student.id })
            assertNull(viewModel.state.value.confirmation)
            assertEquals(listOf(cancelled.id), viewModel.state.value.invites.map { it.classId })

            viewModel.onAccept(cancelled.id)
            assertEquals(MembershipStatus.ACTIVE, world.statusOf(cancelled.id, student.id))
            assertTrue(world.studentClasses(account).state.value.myClasses.none { it.classId == cancelled.id })
        }
    }
}
