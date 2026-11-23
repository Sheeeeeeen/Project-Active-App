package ph.project.active.presentation.coachclasses

import kotlinx.datetime.DayOfWeek
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.model.TimeSlot
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.common.formatDays
import ph.project.active.presentation.common.formatDaysLong
import ph.project.active.presentation.common.formatSlot

data class CoachClassItem(
    val classId: String,
    val name: String,
    val visibility: Visibility,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
    val rosterCount: Int,
    val pendingRequestCount: Int,
    val isCancelled: Boolean,
)

fun CoachClassItem.dayLine(): String = formatDays(slotsByWeekday.keys)

fun CoachClassItem.timeLine(): String =
    slotsByWeekday.values
        .flatten()
        .sortedBy { it.start }
        .distinct()
        .joinToString(", ") { slot -> formatSlot(slot) }

fun CoachClassItem.metaLine(): String {
    val students = UiCopy.studentCount(rosterCount)
    return if (pendingRequestCount > 0) "$students \u00B7 ${UiCopy.requestCount(pendingRequestCount)}" else students
}

fun CoachClassItem.chipLabel(): String = when {
    isCancelled -> UiCopy.cancelled()
    visibility == Visibility.PRIVATE -> UiCopy.visibilityPrivate()
    else -> UiCopy.visibilityPublic()
}

fun CoachClassItem.spokenCard(): String {
    val parts = listOf(
        name,
        chipLabel(),
        formatDaysLong(slotsByWeekday.keys),
        timeLine().replace("\u2013", "to"),
        UiCopy.studentCount(rosterCount),
    )
    val requests = if (pendingRequestCount > 0) listOf(UiCopy.requestCount(pendingRequestCount)) else emptyList()
    return (parts + requests).joinToString(", ")
}
