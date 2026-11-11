package ph.project.active.data.fake

import ph.project.active.data.repository.AttendanceUploader
import ph.project.active.domain.model.AttendanceRecord

class FakeAttendanceUploader(
    private val handler: (AttendanceRecord) -> Boolean = { true },
) : AttendanceUploader {
    val uploaded = mutableListOf<AttendanceRecord>()

    override suspend fun upload(record: AttendanceRecord): Boolean {
        uploaded += record
        return handler(record)
    }
}
