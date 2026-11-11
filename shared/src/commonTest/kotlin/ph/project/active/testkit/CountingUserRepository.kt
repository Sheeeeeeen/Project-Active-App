package ph.project.active.testkit

import ph.project.active.domain.model.User
import ph.project.active.domain.repository.UserRepository

/** Counts employee-id lookups so offline invite search can be shown to touch nothing. */
class CountingUserRepository(private val inner: UserRepository) : UserRepository {
    var employeeIdLookups: Int = 0

    override fun nextId(): String = inner.nextId()

    override fun all(): List<User> = inner.all()

    override fun findById(id: String): User? = inner.findById(id)

    override fun findByEmail(email: String): User? = inner.findByEmail(email)

    override fun findByEmployeeId(employeeId: String): User? {
        employeeIdLookups += 1
        return inner.findByEmployeeId(employeeId)
    }

    override fun add(user: User) {
        inner.add(user)
    }
}
