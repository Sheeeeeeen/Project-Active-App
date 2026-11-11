package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import ph.project.active.domain.InviteResult
import ph.project.active.domain.JoinResult
import ph.project.active.domain.MembershipStatus
import ph.project.active.domain.Visibility
import ph.project.active.domain.activeRoster
import ph.project.active.testkit.FRI_25_SEP_2026
import ph.project.active.testkit.Phase2World
import kotlinx.datetime.atTime

class SqlMembershipParityTest {

    @Test
    fun AC9_requests_invites_and_roster_match_phase1() {
        Phase2World(start = FRI_25_SEP_2026.atTime(9, 0)).use { world ->
            val coach = world.coach()
            val publicClass = world.createMonWedEveningClass(coach.id, name = "Public TRX")
            val privateClass = world.createMonWedEveningClass(coach.id, visibility = Visibility.PRIVATE, name = "Private TRX")
            val student = world.student(1)

            val requested = world.requestToJoin(publicClass.id, student.id)
            assertIs<JoinResult.Requested>(requested)
            assertEquals(JoinResult.AlreadyRequested(requested.membership), world.requestToJoin(publicClass.id, student.id))
            assertEquals(1, world.membershipsOf(publicClass.id, student.id).size)

            world.approveRequest(publicClass.id, student.id)
            assertEquals(MembershipStatus.ACTIVE, world.statusOf(publicClass.id, student.id))
            world.removeFromRoster(publicClass.id, student.id)
            assertEquals(MembershipStatus.REMOVED, world.statusOf(publicClass.id, student.id))
            assertIs<JoinResult.Requested>(world.requestToJoin(publicClass.id, student.id))

            val declined = world.student(2)
            world.requestToJoin(publicClass.id, declined.id)
            world.declineRequest(publicClass.id, declined.id)
            assertEquals(MembershipStatus.DECLINED, world.statusOf(publicClass.id, declined.id))
            assertIs<JoinResult.Requested>(world.requestToJoin(publicClass.id, declined.id))

            assertEquals(JoinResult.PrivateClass, world.requestToJoin(privateClass.id, student.id))
            val invited = assertIs<InviteResult.Invited>(world.inviteByEmployeeId(privateClass.id, "EMP-1"))
            assertEquals(student.id, invited.membership.studentId)
            assertEquals(InviteResult.NoMatch, world.inviteByEmployeeId(privateClass.id, "EMP-404"))
            assertEquals(InviteResult.NoMatch, world.inviteByEmployeeId(privateClass.id, coach.email))
            world.acceptInvite(privateClass.id, student.id)
            assertEquals(MembershipStatus.ACTIVE, world.statusOf(privateClass.id, student.id))

            val other = world.student(3)
            world.inviteByEmployeeId(publicClass.id, "EMP-3")
            world.declineInvite(publicClass.id, other.id)
            assertNotEquals(MembershipStatus.ACTIVE, world.statusOf(publicClass.id, other.id))
            assertTrue(world.memberships.activeRoster(publicClass.id).none { it == other.id })
        }
    }
}

private inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R = try {
    block(this)
} finally {
    close()
}
