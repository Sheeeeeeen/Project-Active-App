package ph.project.active.domain.model

import ph.project.active.domain.repository.current

/** One membership record. A student's latest record for a class is their current status. */
data class Membership(
    val id: String,
    val classId: String,
    val studentId: String,
    val status: MembershipStatus,
)
