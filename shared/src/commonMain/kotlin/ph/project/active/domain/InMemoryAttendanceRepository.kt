package ph.project.active.domain

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
