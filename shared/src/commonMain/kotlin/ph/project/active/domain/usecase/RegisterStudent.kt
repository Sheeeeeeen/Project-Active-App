package ph.project.active.domain.usecase

import ph.project.active.domain.crypto.PasswordHasher
import ph.project.active.domain.repository.UserRepository
import ph.project.active.domain.model.Role
import ph.project.active.domain.model.User

class RegisterStudent(
    private val users: UserRepository,
    private val hasher: PasswordHasher,
    private val requestToJoin: RequestToJoin? = null,
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
        // A failed join does not undo the account that was just created.
        if (requestToJoinClassId != null) {
            checkNotNull(requestToJoin)(classId = requestToJoinClassId, studentId = user.id)
        }
        return user
    }
}
