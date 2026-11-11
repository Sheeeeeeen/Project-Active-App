package ph.project.active.presentation.root

import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.Navigation

data class RootUiState(
    val root: Root,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
