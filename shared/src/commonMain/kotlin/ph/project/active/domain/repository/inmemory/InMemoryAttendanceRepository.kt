package ph.project.active.domain.repository.inmemory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ph.project.active.domain.repository.AttendanceRepository
import ph.project.active.domain.model.AttendanceRecord
import ph.project.active.domain.usecase.SequentialIds

class InMemoryAttendanceRepository : AttendanceRepository {
    private val records = mutableListOf<AttendanceRecord>()
    private val ids = SequentialIds("attendance")
    private val _changes = MutableStateFlow(0)
    override val changes: StateFlow<Int> = _changes.asStateFlow()
    override fun nextId(): String = ids.next()
    override fun all(): List<AttendanceRecord> = records.toList()
    override fun find(sessionId: String, studentId: String): AttendanceRecord? =
        records.firstOrNull { it.sessionId == sessionId && it.studentId == studentId }

    override fun add(record: AttendanceRecord) {
        records += record
        _changes.value += 1
    }

    override fun update(record: AttendanceRecord) {
        val index = records.indexOfFirst { it.id == record.id }
        require(index >= 0) { "Unknown attendance record ${record.id}" }
        records[index] = record
        _changes.value += 1
    }
}
