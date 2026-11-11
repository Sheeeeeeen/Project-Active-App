package ph.project.active.presentation

data class StudentSessionsUiState(
    val upcoming: List<StudentSessionUiRow>,
    val today: List<StudentSessionUiRow>,
    val earlier: List<StudentSessionUiRow>,
    val isEmpty: Boolean,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
