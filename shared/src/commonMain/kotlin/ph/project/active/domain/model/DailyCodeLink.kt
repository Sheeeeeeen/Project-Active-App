package ph.project.active.domain.model

import kotlinx.datetime.LocalDate

/** A daily-code link the phone could read. [sig] is stored for the server; the phone does not check it. */
data class DailyCodeLink(
    val coachId: String,
    val date: LocalDate,
    val rawCoach: String,
    val rawDate: String,
    val sig: String,
)
