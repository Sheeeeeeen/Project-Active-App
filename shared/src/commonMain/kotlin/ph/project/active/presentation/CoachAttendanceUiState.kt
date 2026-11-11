package ph.project.active.presentation

import kotlinx.datetime.LocalDate

data class CoachAttendanceUiState(
    val date: LocalDate,
    val canGoNext: Boolean,
    val sections: List<CoachAttendanceSection>,
    val dayState: DayState,
    val presentCount: Int,
    val absentCount: Int,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
