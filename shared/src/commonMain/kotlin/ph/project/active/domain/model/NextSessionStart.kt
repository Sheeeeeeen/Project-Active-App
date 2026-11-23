package ph.project.active.domain.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime
import kotlinx.datetime.plus

/** The earliest session start at or after midnight on [today], or null when the class has none left. */
fun TrxClass.nextSessionStart(today: LocalDate): LocalDateTime? {
    for (offset in 0 until 7) {
        val date = today.plus(offset, DateTimeUnit.DAY)
        if (endDate != null && date > endDate) return null
        val start = slotsByWeekday[date.dayOfWeek].orEmpty().minOfOrNull { it.start } ?: continue
        return date.atTime(start)
    }
    return null
}
