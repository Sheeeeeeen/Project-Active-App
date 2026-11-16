package ph.project.active.presentation.classdetail

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.TimeSlot
import ph.project.active.domain.model.Visibility
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.common.formatDaysLong
import ph.project.active.presentation.common.formatSlot

data class ClassContent(
    val name: String,
    val visibility: Visibility,
    val coachName: String,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
    val endDate: LocalDate?,
    val isCancelled: Boolean = false,
    val isEnded: Boolean = false,
) {
    fun timeLines(): List<String> {
        val days = slotsByWeekday.keys.sortedBy { it.ordinal }
        if (days.isEmpty()) return emptyList()
        fun slots(day: DayOfWeek) = slotsByWeekday[day].orEmpty().sortedBy { it.start }
        val sameOnEveryDay = days.map { day -> slots(day) }.distinct().size == 1
        if (sameOnEveryDay) {
            return listOf(slots(days.first()).joinToString(", ") { slot -> formatSlot(slot) })
        }
        return days.map { day ->
            val times = slots(day).joinToString(", ") { slot -> formatSlot(slot) }
            "${SHORT_DAYS[day.ordinal]} $times"
        }
    }
}

private val SHORT_DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

fun ClassContent.daysLong(): String = formatDaysLong(slotsByWeekday.keys)

fun ClassContent.runsLine(): String = endDate?.let(UiCopy::runsUntil) ?: UiCopy.noEndDate()

fun ClassContent.endedLine(): String = UiCopy.classEndedOn(checkNotNull(endDate))
