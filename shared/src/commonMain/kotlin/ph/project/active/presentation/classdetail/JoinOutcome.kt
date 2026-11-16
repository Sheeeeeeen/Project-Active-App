package ph.project.active.presentation.classdetail

import kotlinx.serialization.Serializable

@Serializable
enum class JoinOutcome {
    ClassCancelled,
    ClassUnavailable,
}
