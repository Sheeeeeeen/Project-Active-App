package ph.project.active.presentation

data class CoachClassesUiState(
    val classes: List<CoachClassItem>,
    val isEmpty: Boolean,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
