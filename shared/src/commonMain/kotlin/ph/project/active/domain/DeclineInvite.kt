package ph.project.active.domain

/** AC23 */
class DeclineInvite(private val memberships: MembershipRepository) {
    operator fun invoke(classId: String, studentId: String) =
        memberships.transition(classId, studentId, from = MembershipStatus.INVITED, to = MembershipStatus.DECLINED)
}
