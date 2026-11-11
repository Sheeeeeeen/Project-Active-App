package ph.project.active.data

import ph.project.active.domain.Role

data class PersonProfile(
    val id: String,
    val name: String,
    val role: Role,
    val employeeId: String?,
)
