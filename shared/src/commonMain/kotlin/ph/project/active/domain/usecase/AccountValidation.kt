package ph.project.active.domain.usecase

import ph.project.active.domain.repository.UserRepository

internal data class AccountFields(val name: String, val email: String)

internal fun validateAccount(users: UserRepository, name: String, email: String, password: String): AccountFields {
    val cleanName = name.trim()
    val cleanEmail = email.trim()
    require(cleanName.isNotEmpty()) { "Name is required" }
    require(cleanEmail.isNotEmpty()) { "Email is required" }
    require(password.isNotBlank()) { "Password is required" }
    require(users.findByEmail(cleanEmail) == null) { "Email is already registered" }
    return AccountFields(cleanName, cleanEmail)
}
