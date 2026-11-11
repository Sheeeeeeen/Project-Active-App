package ph.project.active.domain

interface AttendanceRepository {
    fun nextId(): String
    fun all(): List<AttendanceRecord>
    fun find(sessionId: String, studentId: String): AttendanceRecord?
    fun add(record: AttendanceRecord)
    fun update(record: AttendanceRecord)
}
