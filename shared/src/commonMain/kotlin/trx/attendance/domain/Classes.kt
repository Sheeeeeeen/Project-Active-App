package trx.attendance.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 * AC7-AC12: saves a class and generates its sessions from today (phone timezone) through
 * [TrxClass.endDate], or through today + [openEndedHorizon] when the class is open-ended.
 * Sessions are numbered 1..n in date, then start-time order.
 */
class CreateClass(
    private val classes: ClassRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
    private val openEndedHorizon: DatePeriod,
) {
    operator fun invoke(
        coachId: String,
        name: String,
        visibility: Visibility,
        slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
        endDate: LocalDate?,
    ): TrxClass {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty()) { "Class name is required" }
        require(slotsByWeekday.values.any { it.isNotEmpty() }) { "Pick at least one weekday and time slot" }

        val today = clock.todayIn(timeZone)
        require(endDate == null || endDate >= today) { "End date cannot be in the past" }

        val trxClass = TrxClass(
            id = classes.nextClassId(),
            coachId = coachId,
            name = cleanName,
            visibility = visibility,
            slotsByWeekday = slotsByWeekday,
            endDate = endDate,
            createdOn = today,
        )
        classes.saveClass(trxClass)
        classes.saveSessions(generateSessions(trxClass, lastDate = endDate ?: today.plus(openEndedHorizon)))
        return trxClass
    }

    private fun generateSessions(trxClass: TrxClass, lastDate: LocalDate): List<Session> {
        val sessions = mutableListOf<Session>()
        var date = trxClass.createdOn
        while (date <= lastDate) {
            for (slot in trxClass.slotsByWeekday[date.dayOfWeek].orEmpty().sortedBy { it.start }) {
                sessions += Session(
                    id = classes.nextSessionId(),
                    classId = trxClass.id,
                    date = date,
                    slot = slot,
                    sessionNumber = sessions.size + 1,
                )
            }
            date = date.plus(1, DateTimeUnit.DAY)
        }
        return sessions
    }
}

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
