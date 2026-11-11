package ph.project.active.presentation

import kotlin.time.Instant

data class RecordedTodayRow(
    val className: String,
    val sessionNumber: Int,
    val scannedAt: Instant,
    val uploadPending: Boolean,
)
