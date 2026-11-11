package ph.project.active.domain.repository.inmemory

import ph.project.active.domain.repository.UserRepository
import ph.project.active.domain.usecase.SequentialIds
import ph.project.active.domain.model.User

class InMemoryUserRepository : UserRepository {
    private val users = mutableListOf<User>()
    private val ids = SequentialIds("user")
    override fun nextId(): String = ids.next()
    override fun all(): List<User> = users.toList()
    override fun findById(id: String): User? = users.firstOrNull { it.id == id }
    override fun findByEmail(email: String): User? = users.firstOrNull { it.email.equals(email, ignoreCase = true) }
    override fun findByEmployeeId(employeeId: String): User? = users.firstOrNull { it.employeeId == employeeId }
    override fun add(user: User) {
        require(findById(user.id) == null) { "Duplicate user id ${user.id}" }
        users += user
    }
}
