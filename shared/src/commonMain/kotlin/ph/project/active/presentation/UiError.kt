package ph.project.active.presentation

sealed interface UiError {
    data object AuthFailed : UiError
    data object Offline : UiError
    data object ClassCancelled : UiError
    data object PrivateClass : UiError
    data object EndDateInPast : UiError
    data object PastDate : UiError
}
