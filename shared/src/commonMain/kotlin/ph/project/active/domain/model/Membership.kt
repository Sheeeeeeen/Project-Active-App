package ph.project.active.domain.model

/** One membership record. A student's latest record for a class is their current status. */
data class Membership(
    val id: String,
    val classId: String,
    val studentId: String,
    val status: MembershipStatus,
    /** Set when the membership turns ACTIVE from a request or an invite. */
    val joinedBy: JoinPath? = null,
)
