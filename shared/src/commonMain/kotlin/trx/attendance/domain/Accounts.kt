package trx.attendance.domain

/** AC1-AC3, AC18: registers a student (signed in immediately) and optionally requests to join a class. */
class RegisterStudent(
    private val users: UserRepository,
    private val hasher: PasswordHasher,
    private val requestToJoin: RequestToJoin,
) {
    operator fun invoke(
        name: String,
        email: String,
        password: String,
        employeeId: String,
        requestToJoinClassId: String? = null,
    ): User {
        val cleanEmployeeId = employeeId.trim()
        require(cleanEmployeeId.isNotEmpty()) { "Employee ID is required" }
        val fields = validateAccount(users, name, email, password)
        require(users.findByEmployeeId(cleanEmployeeId) == null) { "Employee ID is already registered" }

        val user = User(
            id = users.nextId(),
            name = fields.name,
            email = fields.email,
            role = Role.STUDENT,
            employeeId = cleanEmployeeId,
            passwordHash = hasher.hash(password),
        )
        users.add(user)
        // Guest "request to join" path: the result (e.g. ClassCancelled, PrivateClass) does not undo registration.
        if (requestToJoinClassId != null) requestToJoin(classId = requestToJoinClassId, studentId = user.id)
        return user
    }
}

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

private data class AccountFields(val name: String, val email: String)

private fun validateAccount(users: UserRepository, name: String, email: String, password: String): AccountFields {
    val cleanName = name.trim()
    val cleanEmail = email.trim()
    require(cleanName.isNotEmpty()) { "Name is required" }
    require(cleanEmail.isNotEmpty()) { "Email is required" }
    require(password.isNotBlank()) { "Password is required" }
    require(users.findByEmail(cleanEmail) == null) { "Email is already registered" }
    return AccountFields(cleanName, cleanEmail)
}
