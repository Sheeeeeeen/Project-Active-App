package ph.project.active.domain.usecase

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.domain.repository.activeClassIds
import ph.project.active.domain.model.SessionStatus
import ph.project.active.domain.repository.sessionsOn
import ph.project.active.domain.model.Session

/** AC38: today's SCHEDULED sessions (one per slot) on classes the student is ACTIVE on. */
class TodaysChoosableSessions(
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    operator fun invoke(studentId: String): List<Session> {
        val today = clock.todayIn(timeZone)
        return memberships.activeClassIds(studentId)
            .mapNotNull { classes.findClass(it) }
            .filterNot { it.isCancelled }
            .flatMap { classes.sessionsOn(it.id, today) }
            .filter { it.status == SessionStatus.SCHEDULED }
            .sortedWith(SESSION_ORDER)
    }
}
