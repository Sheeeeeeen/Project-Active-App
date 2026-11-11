package trx.attendance.domain

sealed interface JoinResult {
    data class Requested(val membership: Membership) : JoinResult
    data class AlreadyRequested(val membership: Membership) : JoinResult
    data class AlreadyInvited(val membership: Membership) : JoinResult
    data class AlreadyMember(val membership: Membership) : JoinResult
    data object ClassNotFound : JoinResult
    data object ClassCancelled : JoinResult
    data object PrivateClass : JoinResult
}

sealed interface InviteResult {
    data class Invited(val membership: Membership) : InviteResult
    data class AlreadyInvited(val membership: Membership) : InviteResult
    data class AlreadyRequested(val membership: Membership) : InviteResult
    data class AlreadyMember(val membership: Membership) : InviteResult
    /** No student has this employee ID. Coaches have none, so a coach can never be matched (AC22). */
    data object NoMatch : InviteResult
    data object ClassNotFound : InviteResult
    data object ClassCancelled : InviteResult
}

/** AC18-AC21, AC26: a student asks to join a public, open class. */
class RequestToJoin(
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
) {
    operator fun invoke(classId: String, studentId: String): JoinResult {
        val trxClass = classes.findClass(classId) ?: return JoinResult.ClassNotFound
        if (trxClass.isCancelled) return JoinResult.ClassCancelled
        if (trxClass.visibility == Visibility.PRIVATE) return JoinResult.PrivateClass

        val current = memberships.current(classId, studentId)
        when (current?.status) {
            MembershipStatus.PENDING_REQUEST -> return JoinResult.AlreadyRequested(current)
            MembershipStatus.INVITED -> return JoinResult.AlreadyInvited(current)
            MembershipStatus.ACTIVE -> return JoinResult.AlreadyMember(current)
            MembershipStatus.DECLINED, MembershipStatus.REMOVED, null -> Unit
        }
        val membership = Membership(memberships.nextId(), classId, studentId, MembershipStatus.PENDING_REQUEST)
        memberships.add(membership)
        return JoinResult.Requested(membership)
    }
}

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

/** AC20 */
class ApproveRequest(private val memberships: MembershipRepository) {
    operator fun invoke(classId: String, studentId: String) =
        memberships.transition(classId, studentId, from = MembershipStatus.PENDING_REQUEST, to = MembershipStatus.ACTIVE)
}

/** AC20 */
class DeclineRequest(private val memberships: MembershipRepository) {
    operator fun invoke(classId: String, studentId: String) =
        memberships.transition(classId, studentId, from = MembershipStatus.PENDING_REQUEST, to = MembershipStatus.DECLINED)
}

/** AC23 */
class AcceptInvite(private val memberships: MembershipRepository) {
    operator fun invoke(classId: String, studentId: String) =
        memberships.transition(classId, studentId, from = MembershipStatus.INVITED, to = MembershipStatus.ACTIVE)
}

/** AC23 */
class DeclineInvite(private val memberships: MembershipRepository) {
    operator fun invoke(classId: String, studentId: String) =
        memberships.transition(classId, studentId, from = MembershipStatus.INVITED, to = MembershipStatus.DECLINED)
}

/** AC24 */
class RemoveFromRoster(private val memberships: MembershipRepository) {
    operator fun invoke(classId: String, studentId: String) =
        memberships.transition(classId, studentId, from = MembershipStatus.ACTIVE, to = MembershipStatus.REMOVED)
}

/** Moves the student's current membership from [from] to [to]; throws [IllegalStateException] otherwise. */
private fun MembershipRepository.transition(
    classId: String,
    studentId: String,
    from: MembershipStatus,
    to: MembershipStatus,
): Membership {
    val current = current(classId, studentId)
    check(current != null && current.status == from) {
        "Expected membership $from for student $studentId on class $classId but was ${current?.status}"
    }
    val updated = current.copy(status = to)
    update(updated)
    return updated
}
