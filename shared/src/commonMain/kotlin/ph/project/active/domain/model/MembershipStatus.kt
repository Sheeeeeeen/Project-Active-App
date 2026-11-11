package ph.project.active.domain.model

enum class MembershipStatus(val isJoined: Boolean) {
    PENDING_REQUEST(false),
    INVITED(false),
    ACTIVE(true),
    DECLINED(false),
    REMOVED(false),
}
