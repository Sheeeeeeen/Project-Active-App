package ph.project.active.presentation.studentsessions

import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.Navigation

data class StudentSessionsUiState(
    val upcoming: List<StudentSessionUiRow>,
    val today: List<StudentSessionUiRow>,
    val earlier: List<StudentSessionUiRow>,
    val isEmpty: Boolean,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
