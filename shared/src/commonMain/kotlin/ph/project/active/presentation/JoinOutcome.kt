package ph.project.active.presentation

sealed interface JoinOutcome {
    data object ClassCancelled : JoinOutcome
}
