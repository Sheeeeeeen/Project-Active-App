package ph.project.active.domain

/** AC22: the coach invites a student by employee ID (works for public and private classes). */
class InviteByEmployeeId(
    private val users: UserRepository,
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
) {
    operator fun invoke(classId: String, employeeId: String): InviteResult {
        val trxClass = classes.findClass(classId) ?: return InviteResult.ClassNotFound
        if (trxClass.isCancelled) return InviteResult.ClassCancelled

        val student = users.findByEmployeeId(employeeId.trim())
            ?.takeIf { it.role == Role.STUDENT }
            ?: return InviteResult.NoMatch

        val current = memberships.current(classId, student.id)
        when (current?.status) {
            MembershipStatus.INVITED -> return InviteResult.AlreadyInvited(current)
            MembershipStatus.PENDING_REQUEST -> return InviteResult.AlreadyRequested(current)
            MembershipStatus.ACTIVE -> return InviteResult.AlreadyMember(current)
            MembershipStatus.DECLINED, MembershipStatus.REMOVED, null -> Unit
        }
        val membership = Membership(memberships.nextId(), classId, student.id, MembershipStatus.INVITED)
        memberships.add(membership)
        return InviteResult.Invited(membership)
    }
}
