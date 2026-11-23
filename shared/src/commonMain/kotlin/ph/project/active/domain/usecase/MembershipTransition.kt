package ph.project.active.domain.usecase

import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.Membership
import ph.project.active.domain.model.JoinPath
import ph.project.active.domain.repository.current

/** Moves the student's current membership from [from] to [to]; throws [IllegalStateException] otherwise. */
internal fun MembershipRepository.transition(
    classId: String,
    studentId: String,
    from: MembershipStatus,
    to: MembershipStatus,
): Membership {
    val current = current(classId, studentId)
    check(current != null && current.status == from) {
        "Expected membership $from for student $studentId on class $classId but was ${current?.status}"
    }
    val joinedBy = when {
        to != MembershipStatus.ACTIVE -> current.joinedBy
        from == MembershipStatus.PENDING_REQUEST -> JoinPath.REQUEST
        from == MembershipStatus.INVITED -> JoinPath.INVITE
        else -> current.joinedBy
    }
    val updated = current.copy(status = to, joinedBy = joinedBy)
    update(updated)
    return updated
}
