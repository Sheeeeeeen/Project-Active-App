package ph.project.active.domain.model

sealed interface InviteResult {
    data class Invited(val membership: Membership) : InviteResult
    data class AlreadyInvited(val membership: Membership) : InviteResult
    data class AlreadyRequested(val membership: Membership) : InviteResult
    data class AlreadyMember(val membership: Membership) : InviteResult
    /** No student has this employee ID. Coaches have none, so a coach can never be matched (AC22). */
    data object NoMatch : InviteResult
    data object ClassNotFound : InviteResult
    data object ClassCancelled : InviteResult
}
