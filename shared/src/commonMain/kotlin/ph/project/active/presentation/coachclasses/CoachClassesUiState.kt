package ph.project.active.presentation.coachclasses

import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.Navigation

data class CoachClassesUiState(
    val classes: List<CoachClassItem>,
    val isEmpty: Boolean,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
