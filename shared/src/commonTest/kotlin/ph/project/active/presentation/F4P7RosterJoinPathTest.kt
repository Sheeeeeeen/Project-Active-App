package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.domain.model.JoinPath
import ph.project.active.domain.repository.current
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount

class F4P7RosterJoinPathTest {
    @Test
    fun F4_P7_anApprovedRequestAndAnAcceptedInviteShowTheirJoinPath() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id)
            val byRequest = world.student(1)
            world.requestToJoin(trxClass.id, byRequest.id)
            world.approveRequest(trxClass.id, byRequest.id)
            val byInvite = world.student(2)
            world.inviteByEmployeeId(trxClass.id, "EMP-2")
            world.acceptInvite(trxClass.id, byInvite.id)

            val roster = world.manage(coach.toAccount(), trxClass.id).state.value.roster

            assertEquals("Joined by request", roster.single { it.studentId == byRequest.id }.joinedBy)
            assertEquals("Joined by invite", roster.single { it.studentId == byInvite.id }.joinedBy)
            // P7 storage (section 10, Q6): the defaulted Membership.joinedBy set when it turns ACTIVE.
            assertEquals(JoinPath.REQUEST, world.memberships.current(trxClass.id, byRequest.id)?.joinedBy)
            assertEquals(JoinPath.INVITE, world.memberships.current(trxClass.id, byInvite.id)?.joinedBy)
        }
    }
}
