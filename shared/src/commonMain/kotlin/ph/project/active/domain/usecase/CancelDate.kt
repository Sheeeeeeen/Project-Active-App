package ph.project.active.domain.usecase

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.domain.model.SessionStatus
import ph.project.active.domain.repository.sessionsOn

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
