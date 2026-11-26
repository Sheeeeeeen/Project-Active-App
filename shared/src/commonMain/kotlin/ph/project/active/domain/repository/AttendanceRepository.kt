package ph.project.active.domain.repository

import kotlinx.coroutines.flow.StateFlow
import ph.project.active.domain.model.AttendanceRecord

interface AttendanceRepository {
    val changes: StateFlow<Int>
    fun nextId(): String
    fun all(): List<AttendanceRecord>
    fun find(sessionId: String, studentId: String): AttendanceRecord?
    fun add(record: AttendanceRecord)
    fun update(record: AttendanceRecord)
}
