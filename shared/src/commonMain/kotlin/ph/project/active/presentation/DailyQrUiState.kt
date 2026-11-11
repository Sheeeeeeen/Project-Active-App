package ph.project.active.presentation

import kotlinx.datetime.LocalDate

data class DailyQrUiState(
    val today: LocalDate,
    val coachName: String,
    val validFor: LocalDate,
    val code: String,
    val todaysSessions: List<TodaySessionRow>,
    val classListUnavailable: Boolean,
    val error: UiError?,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
