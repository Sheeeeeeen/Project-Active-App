package ph.project.active.domain.model

sealed interface JoinResult {
    data class Requested(val membership: Membership) : JoinResult
    data class AlreadyRequested(val membership: Membership) : JoinResult
    data class AlreadyInvited(val membership: Membership) : JoinResult
    data class AlreadyMember(val membership: Membership) : JoinResult
    data object ClassNotFound : JoinResult
    data object ClassCancelled : JoinResult
    data object PrivateClass : JoinResult
}
