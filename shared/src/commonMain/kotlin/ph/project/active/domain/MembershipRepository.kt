package ph.project.active.domain

interface MembershipRepository {
    fun nextId(): String
    /** Every membership record for [classId], oldest first (history is kept). */
    fun forClass(classId: String): List<Membership>
    fun all(): List<Membership>
    fun add(membership: Membership)
    fun update(membership: Membership)
}

/** The student's current (latest) membership status on a class, or null if none. */
fun MembershipRepository.currentStatus(classId: String, studentId: String): MembershipStatus? =
    current(classId, studentId)?.status

fun MembershipRepository.current(classId: String, studentId: String): Membership? =
    forClass(classId).lastOrNull { it.studentId == studentId }

/** Student ids whose current membership on [classId] is ACTIVE, in the order they first joined. */
fun MembershipRepository.activeRoster(classId: String): List<String> =
    forClass(classId).map { it.studentId }.distinct().filter { currentStatus(classId, it) == MembershipStatus.ACTIVE }

/** Class ids the student is currently ACTIVE on. */
fun MembershipRepository.activeClassIds(studentId: String): Set<String> =
    all().filter { it.studentId == studentId }.map { it.classId }.toSet()
        .filterTo(mutableSetOf()) { currentStatus(it, studentId) == MembershipStatus.ACTIVE }
