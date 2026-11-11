package trx.attendance.domain

import kotlinx.datetime.LocalDate

interface UserRepository {
    /** A new unique, URL-safe user id (it appears in the daily-code URI). */
    fun nextId(): String
    fun all(): List<User>
    fun findById(id: String): User?
    fun findByEmail(email: String): User?
    fun findByEmployeeId(employeeId: String): User?
    fun add(user: User)
}

interface ClassRepository {
    fun nextClassId(): String
    fun nextSessionId(): String
    fun allClasses(): List<TrxClass>
    fun findClass(id: String): TrxClass?
    fun saveClass(trxClass: TrxClass)
    fun sessionsOf(classId: String): List<Session>
    fun findSession(ref: SessionRef): Session?
    fun saveSessions(sessions: List<Session>)
}

interface MembershipRepository {
    fun nextId(): String
    /** Every membership record for [classId], oldest first (history is kept). */
    fun forClass(classId: String): List<Membership>
    fun all(): List<Membership>
    fun add(membership: Membership)
    fun update(membership: Membership)
}

interface AttendanceRepository {
    fun nextId(): String
    fun all(): List<AttendanceRecord>
    fun find(sessionId: String, studentId: String): AttendanceRecord?
    fun add(record: AttendanceRecord)
    fun update(record: AttendanceRecord)
}

interface CoachKeyRepository {
    fun save(coachId: String, keyPair: Ed25519KeyPair)
    fun find(coachId: String): Ed25519KeyPair?
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

fun ClassRepository.sessionsOn(classId: String, date: LocalDate): List<Session> =
    sessionsOf(classId).filter { it.date == date }

// ----- In-memory implementations (Phase 1 local store; SQLDelight replaces them later) -----

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

class InMemoryClassRepository : ClassRepository {
    private val classes = mutableListOf<TrxClass>()
    private val sessions = mutableListOf<Session>()
    private val classIds = SequentialIds("class")
    private val sessionIds = SequentialIds("session")
    override fun nextClassId(): String = classIds.next()
    override fun nextSessionId(): String = sessionIds.next()

    override fun allClasses(): List<TrxClass> = classes.toList()
    override fun findClass(id: String): TrxClass? = classes.firstOrNull { it.id == id }
    override fun saveClass(trxClass: TrxClass) {
        val index = classes.indexOfFirst { it.id == trxClass.id }
        if (index >= 0) classes[index] = trxClass else classes += trxClass
    }

    override fun sessionsOf(classId: String): List<Session> = sessions.filter { it.classId == classId }
    override fun findSession(ref: SessionRef): Session? =
        sessions.firstOrNull { it.classId == ref.classId && it.date == ref.date && it.slot == ref.slot }

    override fun saveSessions(sessions: List<Session>) {
        for (session in sessions) {
            val index = this.sessions.indexOfFirst { it.id == session.id }
            if (index >= 0) this.sessions[index] = session else this.sessions += session
        }
    }
}

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

class InMemoryAttendanceRepository : AttendanceRepository {
    private val records = mutableListOf<AttendanceRecord>()
    private val ids = SequentialIds("attendance")
    override fun nextId(): String = ids.next()
    override fun all(): List<AttendanceRecord> = records.toList()
    override fun find(sessionId: String, studentId: String): AttendanceRecord? =
        records.firstOrNull { it.sessionId == sessionId && it.studentId == studentId }

    override fun add(record: AttendanceRecord) {
        records += record
    }

    override fun update(record: AttendanceRecord) {
        val index = records.indexOfFirst { it.id == record.id }
        require(index >= 0) { "Unknown attendance record ${record.id}" }
        records[index] = record
    }
}

class InMemoryCoachKeyRepository : CoachKeyRepository {
    private val keys = mutableMapOf<String, Ed25519KeyPair>()
    override fun save(coachId: String, keyPair: Ed25519KeyPair) {
        keys[coachId] = keyPair
    }

    override fun find(coachId: String): Ed25519KeyPair? = keys[coachId]
}

/** Deterministic, URL-safe ids for the in-memory store. */
private class SequentialIds(private val prefix: String) {
    private var counter = 0
    fun next(): String = "$prefix-${++counter}"
}
