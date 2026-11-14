package ph.project.active.presentation.classdetail

import kotlinx.datetime.DayOfWeek
import ph.project.active.domain.model.TimeSlot
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.common.formatDays
import ph.project.active.presentation.common.formatSlot

data class RequestingClassHeader(
    val name: String,
    val coachName: String,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
)

fun RequestingClassHeader.scheduleLine(): String {
    val days = formatDays(slotsByWeekday.keys)
    val slots = slotsByWeekday.values
        .flatten()
        .sortedBy { it.start }
        .map { formatSlot(it) }
        .distinct()
        .joinToString(", ")
    return UiCopy.requestingClassSubtitle(coachName, days, slots)
}
