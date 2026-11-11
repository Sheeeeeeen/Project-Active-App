package ph.project.active.presentation.attend

import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import ph.project.active.domain.model.TimeSlot

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
