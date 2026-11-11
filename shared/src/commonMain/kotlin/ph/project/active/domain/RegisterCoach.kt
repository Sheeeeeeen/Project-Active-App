package ph.project.active.domain

/** AC4: registers a coach. Coaches never have an employee ID, so the parameter does not exist. */
class RegisterCoach(
    private val users: UserRepository,
    private val hasher: PasswordHasher,
) {
    operator fun invoke(name: String, email: String, password: String): User {
        val fields = validateAccount(users, name, email, password)
        val user = User(
            id = users.nextId(),
            name = fields.name,
            email = fields.email,
            role = Role.COACH,
            employeeId = null,
            passwordHash = hasher.hash(password),
        )
        users.add(user)
        return user
    }
}
