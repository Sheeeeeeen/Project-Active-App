package ph.project.active.presentation.coachattendance

import kotlinx.datetime.LocalDate
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.createclass.DayState

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
