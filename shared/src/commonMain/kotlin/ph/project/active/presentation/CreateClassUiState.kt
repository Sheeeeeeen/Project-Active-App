package ph.project.active.presentation

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import ph.project.active.domain.TimeSlot
import ph.project.active.domain.Visibility

data class CreateClassUiState(
    val name: String,
    val repeat: RepeatMode,
    val selectedDays: Set<DayOfWeek>,
    val visibility: Visibility,
    val endDate: LocalDate?,
    val daysLocked: Boolean,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
    val slotError: SlotError?,
    val canSave: Boolean,
    val previewDateCount: Int?,
    val savedClassId: String?,
    val error: UiError?,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
