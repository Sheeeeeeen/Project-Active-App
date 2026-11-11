package ph.project.active.presentation

import kotlinx.datetime.LocalDate
import ph.project.active.domain.TimeSlot
import kotlin.time.Instant

sealed interface ScanResult {
    data class Present(
        val className: String,
        val sessionNumber: Int,
        val date: LocalDate,
        val slot: TimeSlot,
        val scannedAt: Instant,
    ) : ScanResult

    data class SavedOffline(
        val className: String,
        val sessionNumber: Int,
        val date: LocalDate,
        val slot: TimeSlot,
        val scannedAt: Instant,
        val uploadPending: Boolean,
    ) : ScanResult

    data class AlreadyRecorded(
        val className: String,
        val sessionNumber: Int,
        val date: LocalDate,
        val firstScannedAt: Instant,
    ) : ScanResult

    data class Rejected(val reason: RejectReason) : ScanResult
}
