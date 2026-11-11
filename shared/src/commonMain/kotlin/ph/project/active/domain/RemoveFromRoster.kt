package ph.project.active.domain

/** AC24 */
class RemoveFromRoster(private val memberships: MembershipRepository) {
    operator fun invoke(classId: String, studentId: String) =
        memberships.transition(classId, studentId, from = MembershipStatus.ACTIVE, to = MembershipStatus.REMOVED)
}
