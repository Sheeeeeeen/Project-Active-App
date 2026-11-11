package ph.project.active.domain.usecase

import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.domain.repository.currentStatus
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.model.TrxClass

/** AC17: public classes for everyone; private classes only for their coach and ACTIVE students. */
class ListVisibleClasses(
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
) {
    operator fun invoke(viewerId: String?): List<TrxClass> = classes.allClasses().filter { trxClass ->
        trxClass.visibility == Visibility.PUBLIC ||
            (viewerId != null && (trxClass.coachId == viewerId ||
                memberships.currentStatus(trxClass.id, viewerId) == MembershipStatus.ACTIVE))
    }
}
