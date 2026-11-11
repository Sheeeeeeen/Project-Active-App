package ph.project.active.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** AC13-AC14: cancels every session of a class on today or a future date. Numbers are kept. */
class CancelDate(
    private val classes: ClassRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    operator fun invoke(classId: String, date: LocalDate) {
        requireNotNull(classes.findClass(classId)) { "Unknown class $classId" }
        require(date >= clock.todayIn(timeZone)) { "Cannot cancel a past date" }
        classes.saveSessions(classes.sessionsOn(classId, date).map { it.copy(status = SessionStatus.CANCELLED) })
    }
}
