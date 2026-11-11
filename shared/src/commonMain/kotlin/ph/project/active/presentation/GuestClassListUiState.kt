package ph.project.active.presentation

data class GuestClassListUiState(
    val items: List<GuestClassItem>,
    val isEmpty: Boolean,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
