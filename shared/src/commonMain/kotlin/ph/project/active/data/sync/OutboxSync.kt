package ph.project.active.data.sync

import ph.project.active.domain.repository.AttendanceRepository
import ph.project.active.data.repository.AttendanceUploader
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.UploadState

class OutboxSync(
    private val attendance: AttendanceRepository,
    private val uploader: AttendanceUploader,
    private val network: NetworkMonitor,
) {
    suspend fun sync(): Int {
        if (network.status.value != NetworkStatus.ONLINE) return 0
        val pending = attendance.all()
            .filter { it.uploadState == UploadState.PENDING }
            .sortedWith(compareBy({ it.scannedAt }, { it.id }))
        var synced = 0
        for (record in pending) {
            val uploaded = uploader.upload(record)
            if (!uploaded) return synced
            attendance.update(record.copy(uploadState = UploadState.SYNCED))
            synced += 1
        }
        return synced
    }
}
