package ph.project.active.presentation.classdetail

sealed interface JoinOutcome {
    data object ClassCancelled : JoinOutcome
}
