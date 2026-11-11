package ph.project.active.presentation.classdetail

import ph.project.active.presentation.manageclass.RequestAction
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.UiError

data class ClassDetailUiState(
    val content: ClassContent?,
    val unavailable: Boolean,
    val requestAction: RequestAction,
    val requestAgain: Boolean,
    val error: UiError?,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
