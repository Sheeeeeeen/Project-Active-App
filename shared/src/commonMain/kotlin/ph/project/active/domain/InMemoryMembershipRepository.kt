package ph.project.active.domain

class InMemoryMembershipRepository : MembershipRepository {
    private val memberships = mutableListOf<Membership>()
    private val ids = SequentialIds("membership")
    override fun nextId(): String = ids.next()
    override fun forClass(classId: String): List<Membership> = memberships.filter { it.classId == classId }
    override fun all(): List<Membership> = memberships.toList()
    override fun add(membership: Membership) {
        memberships += membership
    }

    override fun update(membership: Membership) {
        val index = memberships.indexOfFirst { it.id == membership.id }
        require(index >= 0) { "Unknown membership ${membership.id}" }
        memberships[index] = membership
    }
}
