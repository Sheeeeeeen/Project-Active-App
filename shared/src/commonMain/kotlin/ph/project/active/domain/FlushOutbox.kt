package ph.project.active.domain

/** AC41: while ONLINE, marks pending records SYNCED (scannedAt is untouched). Offline: no-op. */
class FlushOutbox(
    private val attendance: AttendanceRepository,
    private val networkStatus: () -> NetworkStatus,
) {
    /** Returns how many records were marked SYNCED. */
    operator fun invoke(): Int {
        if (networkStatus() != NetworkStatus.ONLINE) return 0
        val pending = attendance.all().filter { it.uploadState == UploadState.PENDING }
        pending.forEach { attendance.update(it.copy(uploadState = UploadState.SYNCED)) }
        return pending.size
    }
}
