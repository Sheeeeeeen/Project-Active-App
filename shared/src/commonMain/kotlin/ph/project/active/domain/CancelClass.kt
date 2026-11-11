package ph.project.active.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** AC15-AC16: cancels the whole class. Today and future sessions are cancelled; the past is kept. */
class CancelClass(
    private val classes: ClassRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    operator fun invoke(classId: String) {
        val trxClass = requireNotNull(classes.findClass(classId)) { "Unknown class $classId" }
        if (trxClass.isCancelled) return
        val today = clock.todayIn(timeZone)
        classes.saveClass(trxClass.copy(cancelledAt = clock.now()))
        classes.saveSessions(
            classes.sessionsOf(classId).filter { it.date >= today }.map { it.copy(status = SessionStatus.CANCELLED) },
        )
    }
}
