package ph.project.active.presentation

sealed interface UiError {
    data object AuthFailed : UiError
    data object Offline : UiError
    data object ClassCancelled : UiError
    data object PrivateClass : UiError
    data object EndDateInPast : UiError
    data object PastDate : UiError

    /** A use case rejected the action because the stored state was not the one the action required. */
    data object InvalidState : UiError
}
