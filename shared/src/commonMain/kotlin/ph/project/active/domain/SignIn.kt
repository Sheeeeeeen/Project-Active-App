package ph.project.active.domain

/** AC5: returns the user on a match; [AuthFailed] for an unknown email or wrong password. */
class SignIn(
    private val users: UserRepository,
    private val hasher: PasswordHasher,
) {
    operator fun invoke(email: String, password: String): User {
        val user = users.findByEmail(email.trim()) ?: throw AuthFailed()
        if (!hasher.verify(password, user.passwordHash)) throw AuthFailed()
        return user
    }
}
