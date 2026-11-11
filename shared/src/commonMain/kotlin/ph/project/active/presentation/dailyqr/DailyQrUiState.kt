package ph.project.active.presentation.dailyqr

import kotlinx.datetime.LocalDate
import ph.project.active.presentation.attend.TodaySessionRow
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.UiError

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
