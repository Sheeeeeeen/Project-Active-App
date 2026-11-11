package ph.project.active.data.model

import ph.project.active.domain.model.Role

data class PersonProfile(
    val id: String,
    val name: String,
    val role: Role,
    val employeeId: String?,
)
