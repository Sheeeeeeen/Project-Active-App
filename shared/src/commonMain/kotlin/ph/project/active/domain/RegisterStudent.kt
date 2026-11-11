package ph.project.active.domain

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
