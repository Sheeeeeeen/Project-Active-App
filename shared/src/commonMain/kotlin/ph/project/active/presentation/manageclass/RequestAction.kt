package ph.project.active.presentation.manageclass

import ph.project.active.domain.usecase.RequestToJoin

sealed interface RequestAction {
    data class RequestToJoin(val opensRegistration: Boolean) : RequestAction
    data object Pending : RequestAction
    data object Joined : RequestAction
    data object Invited : RequestAction
    data object None : RequestAction
}
