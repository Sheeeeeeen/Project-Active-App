package ph.project.active.domain

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/** Named `TrxClass` so it never clashes with `java.lang.Class`. */
data class TrxClass(
    val id: String,
    val coachId: String,
    val name: String,
    val visibility: Visibility,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
    /** `null` means open-ended: the class stays open until cancelled. */
    val endDate: LocalDate?,
    /** Local date (phone timezone) the class was saved on. */
    val createdOn: LocalDate,
    val cancelledAt: Instant? = null,
) {
    val isCancelled: Boolean get() = cancelledAt != null
}
