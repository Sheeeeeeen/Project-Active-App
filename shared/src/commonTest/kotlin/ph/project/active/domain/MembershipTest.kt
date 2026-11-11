package ph.project.active.domain

import kotlinx.datetime.atTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import ph.project.active.domain.model.InviteResult
import ph.project.active.domain.model.JoinResult
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.Visibility

class MembershipTest {

    private class Setup {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach(1)
        val publicClass = world.createMonWedEveningClass(coach.id, visibility = Visibility.PUBLIC, name = "Public TRX")
        val privateClass = world.createMonWedEveningClass(coach.id, visibility = Visibility.PRIVATE, name = "Private TRX")
    }

    // ----- AC17 -----

    @Test
    fun AC17_guest_sees_only_public_classes() {
        val s = Setup()

        val visible = s.world.listVisibleClasses(viewerId = null)

        assertEquals(listOf(s.publicClass.id), visible.map { it.id })
    }

    @Test
    fun AC17_private_class_is_visible_to_its_coach_and_active_students_only() {
        val s = Setup()
        val otherCoach = s.world.coach(2)
        val active = s.world.student(1)
        val invited = s.world.student(2)
        val stranger = s.world.student(3)
        s.world.makeActive(s.privateClass.id, active.id)
        s.world.inviteByEmployeeId(classId = s.privateClass.id, employeeId = "EMP-2")

        fun visibleTo(id: String) = s.world.listVisibleClasses(viewerId = id).map { it.id }.toSet()

        assertEquals(setOf(s.publicClass.id, s.privateClass.id), visibleTo(s.coach.id))
        assertEquals(setOf(s.publicClass.id, s.privateClass.id), visibleTo(active.id))
        assertEquals(setOf(s.publicClass.id), visibleTo(invited.id))
        assertEquals(setOf(s.publicClass.id), visibleTo(stranger.id))
        assertEquals(setOf(s.publicClass.id), visibleTo(otherCoach.id))
    }

    @Test
    fun AC17_private_class_is_hidden_again_after_removal() {
        val s = Setup()
        val student = s.world.student(1)
        s.world.makeActive(s.privateClass.id, student.id)
        s.world.removeFromRoster(classId = s.privateClass.id, studentId = student.id)

        assertEquals(listOf(s.publicClass.id), s.world.listVisibleClasses(viewerId = student.id).map { it.id })
    }

    // ----- AC18 -----

    @Test
    fun AC18_register_student_from_guest_request_path_creates_exactly_one_pending_request() {
        val s = Setup()

        val student = s.world.student(1, targetClassId = s.publicClass.id)

        val created = s.world.membershipsOf(s.publicClass.id, student.id)
        assertEquals(1, created.size)
        assertEquals(MembershipStatus.PENDING_REQUEST, created.single().status)
        assertTrue(s.world.memberships.forClass(s.privateClass.id).isEmpty())
    }

    @Test
    fun AC18_register_student_without_target_class_creates_no_request() {
        val s = Setup()

        s.world.student(1)

        assertTrue(s.world.memberships.forClass(s.publicClass.id).isEmpty())
    }

    @Test
    fun AC18_failed_registration_from_guest_path_creates_no_request() {
        val s = Setup()
        s.world.student(1)

        kotlin.test.assertFailsWith<IllegalArgumentException> {
            s.world.registerStudent(
                name = "Dup",
                email = "student1@trx.test",
                password = "pw-dup-1",
                employeeId = "EMP-77",
                requestToJoinClassId = s.publicClass.id,
            )
        }
        assertTrue(s.world.memberships.forClass(s.publicClass.id).isEmpty())
    }

    // ----- AC19 -----

    @Test
    fun AC19_signed_in_student_request_on_public_class_creates_one_pending_request() {
        val s = Setup()
        val student = s.world.student(1)

        val result = s.world.requestToJoin(classId = s.publicClass.id, studentId = student.id)

        val requested = assertIs<JoinResult.Requested>(result)
        assertEquals(MembershipStatus.PENDING_REQUEST, requested.membership.status)
        assertEquals(s.publicClass.id, requested.membership.classId)
        assertEquals(student.id, requested.membership.studentId)
        assertEquals(listOf(requested.membership), s.world.membershipsOf(s.publicClass.id, student.id))
    }

    @Test
    fun AC19_second_request_while_pending_does_not_create_another() {
        val s = Setup()
        val student = s.world.student(1)

        s.world.requestToJoin(classId = s.publicClass.id, studentId = student.id)
        s.world.requestToJoin(classId = s.publicClass.id, studentId = student.id)

        val pending = s.world.membershipsOf(s.publicClass.id, student.id)
            .filter { it.status == MembershipStatus.PENDING_REQUEST }
        assertEquals(1, pending.size)
        assertEquals(1, s.world.membershipsOf(s.publicClass.id, student.id).size)
    }

    @Test
    fun AC19_guest_path_then_signed_in_request_still_has_one_pending_request() {
        val s = Setup()
        val student = s.world.student(1, targetClassId = s.publicClass.id)

        s.world.requestToJoin(classId = s.publicClass.id, studentId = student.id)

        assertEquals(1, s.world.membershipsOf(s.publicClass.id, student.id).size)
    }

    // ----- AC20 -----

    @Test
    fun AC20_approve_request_sets_membership_active() {
        val s = Setup()
        val student = s.world.student(1)
        s.world.requestToJoin(classId = s.publicClass.id, studentId = student.id)

        s.world.approveRequest(classId = s.publicClass.id, studentId = student.id)

        assertEquals(MembershipStatus.ACTIVE, s.world.statusOf(s.publicClass.id, student.id))
    }

    @Test
    fun AC20_decline_request_sets_declined_and_leaves_student_off_roster() {
        val s = Setup()
        val student = s.world.student(1)
        s.world.requestToJoin(classId = s.publicClass.id, studentId = student.id)

        s.world.declineRequest(classId = s.publicClass.id, studentId = student.id)

        assertEquals(MembershipStatus.DECLINED, s.world.statusOf(s.publicClass.id, student.id))
        assertTrue(s.world.memberships.forClass(s.publicClass.id).none { it.status == MembershipStatus.ACTIVE })
    }

    // ----- AC21 -----

    @Test
    fun AC21_after_declined_request_to_join_creates_a_new_pending_request() {
        val s = Setup()
        val student = s.world.student(1)
        s.world.requestToJoin(classId = s.publicClass.id, studentId = student.id)
        s.world.declineRequest(classId = s.publicClass.id, studentId = student.id)

        val result = s.world.requestToJoin(classId = s.publicClass.id, studentId = student.id)

        assertIs<JoinResult.Requested>(result)
        assertEquals(MembershipStatus.PENDING_REQUEST, s.world.statusOf(s.publicClass.id, student.id))
        assertEquals(
            1,
            s.world.membershipsOf(s.publicClass.id, student.id).count { it.status == MembershipStatus.PENDING_REQUEST },
        )
    }

    @Test
    fun AC21_after_removed_request_to_join_creates_a_new_pending_request() {
        val s = Setup()
        val student = s.world.student(1)
        s.world.makeActive(s.publicClass.id, student.id)
        s.world.removeFromRoster(classId = s.publicClass.id, studentId = student.id)

        val result = s.world.requestToJoin(classId = s.publicClass.id, studentId = student.id)

        assertIs<JoinResult.Requested>(result)
        assertEquals(MembershipStatus.PENDING_REQUEST, s.world.statusOf(s.publicClass.id, student.id))
    }

    // ----- AC22 -----

    @Test
    fun AC22_invite_with_one_matching_student_creates_invited_membership() {
        val s = Setup()
        val student = s.world.student(1)
        s.world.student(2)

        val result = s.world.inviteByEmployeeId(classId = s.privateClass.id, employeeId = "EMP-1")

        val invited = assertIs<InviteResult.Invited>(result)
        assertEquals(MembershipStatus.INVITED, invited.membership.status)
        assertEquals(student.id, invited.membership.studentId)
        assertEquals(s.privateClass.id, invited.membership.classId)
        assertEquals(listOf(invited.membership), s.world.memberships.forClass(s.privateClass.id))
    }

    @Test
    fun AC22_invite_works_on_public_classes_too() {
        val s = Setup()
        val student = s.world.student(1)

        assertIs<InviteResult.Invited>(s.world.inviteByEmployeeId(classId = s.publicClass.id, employeeId = "EMP-1"))
        assertEquals(MembershipStatus.INVITED, s.world.statusOf(s.publicClass.id, student.id))
    }

    @Test
    fun AC22_unknown_employee_id_creates_nothing_and_returns_NoMatch() {
        val s = Setup()
        s.world.student(1)

        val result = s.world.inviteByEmployeeId(classId = s.privateClass.id, employeeId = "EMP-404")

        assertEquals(InviteResult.NoMatch, result)
        assertTrue(s.world.memberships.forClass(s.privateClass.id).isEmpty())
    }

    @Test
    fun AC22_a_coach_cannot_be_invited() {
        val s = Setup()
        val otherCoach = s.world.coach(2)

        // Coaches have no employee ID, so no lookup value can resolve to a coach.
        for (lookup in listOf(otherCoach.id, otherCoach.email, otherCoach.name, s.coach.id, s.coach.email)) {
            assertEquals(InviteResult.NoMatch, s.world.inviteByEmployeeId(classId = s.privateClass.id, employeeId = lookup))
        }
        assertTrue(s.world.memberships.forClass(s.privateClass.id).isEmpty())
    }

    // ----- AC23 -----

    @Test
    fun AC23_accept_invite_sets_active() {
        val s = Setup()
        val student = s.world.student(1)
        s.world.inviteByEmployeeId(classId = s.privateClass.id, employeeId = "EMP-1")

        s.world.acceptInvite(classId = s.privateClass.id, studentId = student.id)

        assertEquals(MembershipStatus.ACTIVE, s.world.statusOf(s.privateClass.id, student.id))
    }

    @Test
    fun AC23_decline_invite_does_not_put_student_on_roster() {
        val s = Setup()
        val student = s.world.student(1)
        s.world.inviteByEmployeeId(classId = s.privateClass.id, employeeId = "EMP-1")

        s.world.declineInvite(classId = s.privateClass.id, studentId = student.id)

        val status = s.world.statusOf(s.privateClass.id, student.id)
        assertNotEquals(MembershipStatus.ACTIVE, status)
        assertNotEquals(MembershipStatus.INVITED, status)
        assertTrue(s.world.memberships.forClass(s.privateClass.id).none { it.status == MembershipStatus.ACTIVE })
    }

    // ----- AC24 -----

    @Test
    fun AC24_remove_from_roster_sets_removed_and_student_can_request_again() {
        val s = Setup()
        val student = s.world.student(1)
        s.world.makeActive(s.publicClass.id, student.id)

        s.world.removeFromRoster(classId = s.publicClass.id, studentId = student.id)

        assertEquals(MembershipStatus.REMOVED, s.world.statusOf(s.publicClass.id, student.id))
        assertIs<JoinResult.Requested>(s.world.requestToJoin(classId = s.publicClass.id, studentId = student.id))
        assertEquals(MembershipStatus.PENDING_REQUEST, s.world.statusOf(s.publicClass.id, student.id))
    }

    // ----- AC25 -----

    @Test
    fun AC25_only_active_membership_counts_as_joined() {
        assertTrue(MembershipStatus.ACTIVE.isJoined)
        assertFalse(MembershipStatus.PENDING_REQUEST.isJoined)
        assertFalse(MembershipStatus.INVITED.isJoined)
        assertFalse(MembershipStatus.DECLINED.isJoined)
        assertFalse(MembershipStatus.REMOVED.isJoined)
    }

    @Test
    fun AC25_joined_follows_the_membership_lifecycle() {
        val s = Setup()
        val student = s.world.student(1)
        fun joined() = s.world.statusOf(s.publicClass.id, student.id)?.isJoined == true

        s.world.requestToJoin(classId = s.publicClass.id, studentId = student.id)
        assertFalse(joined())
        s.world.approveRequest(classId = s.publicClass.id, studentId = student.id)
        assertTrue(joined())
        s.world.removeFromRoster(classId = s.publicClass.id, studentId = student.id)
        assertFalse(joined())
    }

    // ----- AC26 -----

    @Test
    fun AC26_request_to_join_private_class_is_rejected() {
        val s = Setup()
        val student = s.world.student(1)

        val result = s.world.requestToJoin(classId = s.privateClass.id, studentId = student.id)

        assertEquals(JoinResult.PrivateClass, result)
        assertTrue(s.world.memberships.forClass(s.privateClass.id).isEmpty())
    }

    @Test
    fun AC26_guest_registration_targeting_private_class_creates_no_request() {
        val s = Setup()

        s.world.student(1, targetClassId = s.privateClass.id)

        assertTrue(s.world.memberships.forClass(s.privateClass.id).isEmpty())
    }

    @Test
    fun AC26_private_class_is_joinable_by_invite() {
        val s = Setup()
        val student = s.world.student(1)

        s.world.inviteByEmployeeId(classId = s.privateClass.id, employeeId = "EMP-1")
        s.world.acceptInvite(classId = s.privateClass.id, studentId = student.id)

        assertEquals(MembershipStatus.ACTIVE, s.world.statusOf(s.privateClass.id, student.id))
    }
}
