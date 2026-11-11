package ph.project.active.data.model

import ph.project.active.domain.model.Role

data class SignedInAccount(
    val id: String,
    val name: String,
    val email: String,
    val role: Role,
    val employeeId: String?,
)
