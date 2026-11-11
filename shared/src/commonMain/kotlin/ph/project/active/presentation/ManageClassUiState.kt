package ph.project.active.presentation

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import ph.project.active.domain.TimeSlot
import ph.project.active.domain.Visibility

data class ManageClassUiState(
    val name: String,
    val visibility: Visibility,
    val endDate: LocalDate?,
    val sessionCount: Int,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
    val pendingRequestCount: Int,
    val selectedTab: ManageTab,
    val schedule: List<ScheduleRow>,
    val confirmCancelDate: LocalDate?,
    val confirmation: ManageConfirmation?,
    val requests: List<RequestRow>,
    val requestsEmpty: Boolean,
    val roster: List<RosterRow>,
    val confirmRemove: ConfirmRemove?,
    val searchText: String,
    val match: InviteMatch?,
    val canSendInvite: Boolean,
    val noMatch: Boolean,
    val searchedId: String?,
    val optionsOpen: Boolean,
    val confirmCancelClass: Boolean,
    val error: UiError?,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
