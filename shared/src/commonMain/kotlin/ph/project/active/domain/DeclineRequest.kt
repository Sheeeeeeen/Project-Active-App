package ph.project.active.domain

/** AC20 */
class DeclineRequest(private val memberships: MembershipRepository) {
    operator fun invoke(classId: String, studentId: String) =
        memberships.transition(classId, studentId, from = MembershipStatus.PENDING_REQUEST, to = MembershipStatus.DECLINED)
}
