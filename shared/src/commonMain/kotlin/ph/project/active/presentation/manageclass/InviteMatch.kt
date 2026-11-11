package ph.project.active.presentation.manageclass

import ph.project.active.domain.model.MembershipStatus

data class InviteMatch(
    val name: String,
    val employeeId: String,
    val currentMembershipStatus: MembershipStatus?,
)
