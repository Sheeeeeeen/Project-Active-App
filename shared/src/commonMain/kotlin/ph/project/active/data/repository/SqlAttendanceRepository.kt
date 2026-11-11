package ph.project.active.data.repository

import kotlinx.datetime.Instant
import ph.project.active.data.db.TrxDatabase
import ph.project.active.domain.repository.AttendanceRepository
import ph.project.active.domain.model.AttendanceRecord
import ph.project.active.domain.model.AttendanceStatus
import ph.project.active.domain.model.UploadState

class SqlAttendanceRepository(private val database: TrxDatabase) : AttendanceRepository {
    private val ids = SqlIdSource(database)

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
        )
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
            record.id,
        )
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
)

private fun ph.project.active.data.db.SelectAttendanceByPair.toRecord(): AttendanceRecord = AttendanceRecord(
    id = id,
    sessionId = sessionId,
    classId = classId,
    studentId = studentId,
    scannedAt = Instant.parse(scannedAt),
    status = AttendanceStatus.valueOf(status),
    uploadState = UploadState.valueOf(uploadState),
)
