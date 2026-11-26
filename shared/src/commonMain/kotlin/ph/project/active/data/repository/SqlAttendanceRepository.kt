package ph.project.active.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant
import ph.project.active.data.db.TrxDatabase
import ph.project.active.domain.repository.AttendanceRepository
import ph.project.active.domain.model.AttendanceRecord
import ph.project.active.domain.model.AttendanceStatus
import ph.project.active.domain.model.UploadState

class SqlAttendanceRepository(private val database: TrxDatabase) : AttendanceRepository {
    private val ids = SqlIdSource(database)
    private val _changes = MutableStateFlow(0)
    override val changes: StateFlow<Int> = _changes.asStateFlow()

    override fun nextId(): String = ids.next("attendance")

    override fun all(): List<AttendanceRecord> =
        database.trxDatabaseQueries.selectAllAttendance().executeAsList().map { it.toRecord() }

    override fun find(sessionId: String, studentId: String): AttendanceRecord? =
        database.trxDatabaseQueries.selectAttendanceByPair(sessionId, studentId).executeAsOneOrNull()?.toRecord()

    override fun add(record: AttendanceRecord) {
        database.trxDatabaseQueries.insertAttendance(
            record.id,
            record.sessionId,
            record.classId,
            record.studentId,
            record.scannedAt.toString(),
            record.status.name,
            record.uploadState.name,
            record.codeCoach,
            record.codeDate,
            record.codeSig,
        )
        _changes.value += 1
    }

    override fun update(record: AttendanceRecord) {
        val known = database.trxDatabaseQueries.selectAllAttendance().executeAsList().any { it.id == record.id }
        if (!known) throw IllegalArgumentException("Unknown attendance record ${record.id}")
        database.trxDatabaseQueries.updateAttendance(
            record.sessionId,
            record.classId,
            record.studentId,
            record.scannedAt.toString(),
            record.status.name,
            record.uploadState.name,
            record.codeCoach,
            record.codeDate,
            record.codeSig,
            record.id,
        )
        _changes.value += 1
    }
}

private fun ph.project.active.data.db.SelectAllAttendance.toRecord(): AttendanceRecord = AttendanceRecord(
    id = id,
    sessionId = sessionId,
    classId = classId,
    studentId = studentId,
    scannedAt = Instant.parse(scannedAt),
    status = AttendanceStatus.valueOf(status),
    uploadState = UploadState.valueOf(uploadState),
    codeCoach = codeCoach,
    codeDate = codeDate,
    codeSig = codeSig,
)

private fun ph.project.active.data.db.SelectAttendanceByPair.toRecord(): AttendanceRecord = AttendanceRecord(
    id = id,
    sessionId = sessionId,
    classId = classId,
    studentId = studentId,
    scannedAt = Instant.parse(scannedAt),
    status = AttendanceStatus.valueOf(status),
    uploadState = UploadState.valueOf(uploadState),
    codeCoach = codeCoach,
    codeDate = codeDate,
    codeSig = codeSig,
)
