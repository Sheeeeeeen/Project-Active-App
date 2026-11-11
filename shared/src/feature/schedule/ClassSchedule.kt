package ph.com.sheen.projectactive.feature.schedule

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import ph.com.sheen.projectactive.feature.classes.Class

class ClassSchedule {
    private val cancelled = mutableSetOf<Pair<String, LocalDate>>()

    fun cancel(created: Class, date: LocalDate) {
        cancelled += created.name to date
    }

    fun sessions(created: Class, savedOn: LocalDate, through: LocalDate): List<Session> {
        val last = created.end?.let { minOf(it, through) } ?: through
        if (savedOn > last) return emptyList()
        val weekdays = created.days.map { DayOfWeek.valueOf(it.name.uppercase()) }.toSet()
        val sessions = mutableListOf<Session>()
        var date = savedOn
        while (date <= last) {
            val open = created.everyDay || date.dayOfWeek in weekdays
            if (open) {
                val status = if ((created.name to date) in cancelled) {
                    SessionStatus.Cancelled
                } else {
                    SessionStatus.Scheduled
                }
                sessions += Session(date, status)
            }
            date = date.plus(1, DateTimeUnit.DAY)
        }
        return sessions
    }
}
