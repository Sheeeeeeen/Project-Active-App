package ph.project.active.presentation.common

import ph.project.active.domain.model.AuthFailed

sealed interface UiError {
    data object AuthFailed : UiError
    data object Offline : UiError
    data object ClassCancelled : UiError
    data object PrivateClass : UiError
    data object EndDateInPast : UiError
    data object PastDate : UiError
    data object InvalidSlot : UiError
    data object AlreadyInvited : UiError
    data object AlreadyRequested : UiError
    data object AlreadyMember : UiError

    /** A use case rejected the action because the stored state was not the one the action required. */
    data object InvalidState : UiError
}
