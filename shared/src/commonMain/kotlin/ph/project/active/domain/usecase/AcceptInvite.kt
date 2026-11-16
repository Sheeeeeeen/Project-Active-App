package ph.project.active.domain.usecase

import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.domain.model.MembershipStatus

class AcceptInvite(private val memberships: MembershipRepository) {
    operator fun invoke(classId: String, studentId: String) =
        memberships.transition(classId, studentId, from = MembershipStatus.INVITED, to = MembershipStatus.ACTIVE)
}
