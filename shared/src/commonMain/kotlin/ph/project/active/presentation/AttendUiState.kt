package ph.project.active.presentation

import kotlinx.datetime.LocalDate

data class AttendUiState(
    val today: LocalDate,
    val sheetOpen: Boolean,
    val choices: List<AttendChoice>,
    val noSessionsToday: Boolean,
    val canScan: Boolean,
    val selectedSessionId: String?,
    val scannerOpen: Boolean,
    val recordingFor: RecordingFor?,
    val result: ScanResult?,
    val recordedToday: List<RecordedTodayRow>,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
