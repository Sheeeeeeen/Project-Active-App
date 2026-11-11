package ph.project.active.presentation

data class ClassDetailUiState(
    val content: ClassContent?,
    val unavailable: Boolean,
    val requestAction: RequestAction,
    val requestAgain: Boolean,
    val error: UiError?,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
