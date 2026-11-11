package ph.project.active.presentation

import ph.project.active.domain.MembershipStatus

data class InviteMatch(
    val name: String,
    val employeeId: String,
    val currentMembershipStatus: MembershipStatus?,
)
