package ph.project.active.data.repository

import ph.project.active.domain.model.AttendanceRecord

interface AttendanceUploader {
    suspend fun upload(record: AttendanceRecord): Boolean
}
