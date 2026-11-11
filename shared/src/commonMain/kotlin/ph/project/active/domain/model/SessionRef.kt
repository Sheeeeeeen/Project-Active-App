package ph.project.active.domain.model

import kotlinx.datetime.LocalDate

/** What the student chose before scanning: class + date + slot. */
data class SessionRef(val classId: String, val date: LocalDate, val slot: TimeSlot)
