package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.common.UiError
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount

class F4P4OpenErrorLinesTest {
    @Test
    fun F4_P4_theFourNewErrorsMapToTheQuestion8Lines() {
        assertEquals("The end time must be after the start time.", UiCopy.uiError(UiError.InvalidSlot))
        assertEquals("This student already has an invite.", UiCopy.uiError(UiError.AlreadyInvited))
        assertEquals("This student already requested this class.", UiCopy.uiError(UiError.AlreadyRequested))
        assertEquals("This student is already on the roster.", UiCopy.uiError(UiError.AlreadyMember))
    }

    @Test
    fun F4_P4_sendingToAnInvitedStudentSetsAlreadyInvitedAndAddsNothing() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id)
            val student = world.student(7)
            world.inviteByEmployeeId(trxClass.id, "EMP-7")
            val before = world.memberships.all()

            val viewModel = world.manage(coach.toAccount(), trxClass.id)
            viewModel.onSearchTextChange("EMP-7")
            viewModel.onSendInvite()

            assertEquals(UiError.AlreadyInvited, viewModel.state.value.error)
            assertEquals(MembershipStatus.INVITED, viewModel.state.value.match?.currentMembershipStatus)
            assertEquals(before, world.memberships.all())
            assertEquals(MembershipStatus.INVITED, world.statusOf(trxClass.id, student.id))
        }
    }

    @Test
    fun F4_P4_sendingToARequestedOrActiveStudentSetsTheMatchingError() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id)
            val requester = world.student(8)
            world.requestToJoin(trxClass.id, requester.id)
            val member = world.student(9)
            world.makeActive(trxClass.id, member.id)
            val before = world.memberships.all()
            val viewModel = world.manage(coach.toAccount(), trxClass.id)

            viewModel.onSearchTextChange("EMP-8")
            viewModel.onSendInvite()
            assertEquals(UiError.AlreadyRequested, viewModel.state.value.error)
            assertEquals(MembershipStatus.PENDING_REQUEST, viewModel.state.value.match?.currentMembershipStatus)

            viewModel.onSearchTextChange("EMP-9")
            viewModel.onSendInvite()
            assertEquals(UiError.AlreadyMember, viewModel.state.value.error)
            assertEquals(MembershipStatus.ACTIVE, viewModel.state.value.match?.currentMembershipStatus)

            assertEquals(before, world.memberships.all())
        }
    }
}
