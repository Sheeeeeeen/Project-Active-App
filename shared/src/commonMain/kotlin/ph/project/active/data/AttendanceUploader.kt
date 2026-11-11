package ph.project.active.data

import ph.project.active.domain.AttendanceRecord

interface AttendanceUploader {
    suspend fun upload(record: AttendanceRecord): Boolean
}
