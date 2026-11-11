package ph.project.active.presentation.createclass

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.Navigation
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.model.TimeSlot
import ph.project.active.presentation.common.UiError

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
