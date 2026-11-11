package ph.project.active.domain

interface UserRepository {
    /** A new unique, URL-safe user id (it appears in the daily-code URI). */
    fun nextId(): String
    fun all(): List<User>
    fun findById(id: String): User?
    fun findByEmail(email: String): User?
    fun findByEmployeeId(employeeId: String): User?
    fun add(user: User)
}
