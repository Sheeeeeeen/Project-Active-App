package ph.project.active.data

import ph.project.active.domain.AttendanceRecord

class FakeAttendanceUploader(
    private val handler: (AttendanceRecord) -> Boolean = { true },
) : AttendanceUploader {
    val uploaded = mutableListOf<AttendanceRecord>()

    override suspend fun upload(record: AttendanceRecord): Boolean {
        uploaded += record
        return handler(record)
    }
}
