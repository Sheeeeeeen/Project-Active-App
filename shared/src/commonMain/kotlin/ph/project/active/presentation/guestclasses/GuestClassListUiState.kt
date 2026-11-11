package ph.project.active.presentation.guestclasses

import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.Navigation

data class GuestClassListUiState(
    val items: List<GuestClassItem>,
    val isEmpty: Boolean,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
