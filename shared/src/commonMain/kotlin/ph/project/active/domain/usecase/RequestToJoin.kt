package ph.project.active.domain.usecase

import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.domain.model.JoinResult
import ph.project.active.domain.model.Membership
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.repository.current

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
