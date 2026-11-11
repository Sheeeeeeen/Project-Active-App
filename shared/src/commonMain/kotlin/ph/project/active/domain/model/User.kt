package ph.project.active.domain.model

/** A registered account. The role is fixed at creation (`val`, no setter, no use case changes it). */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: Role,
    /** Students only. Coaches always have `null`. */
    val employeeId: String?,
    /** Only the hash is kept; the plain-text password never reaches the domain model. */
    val passwordHash: String,
) {
    override fun toString(): String =
        "User(id=$id, name=$name, email=$email, role=$role, employeeId=$employeeId, passwordHash=***)"
}
