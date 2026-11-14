package ph.project.active.presentation.manageclass

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.createclass.ScheduleRow
import ph.project.active.presentation.common.Navigation
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.model.TimeSlot
import ph.project.active.presentation.common.UiError

data class ManageClassUiState(
    val connection: ConnectionUi,
    val name: String = "",
    val visibility: Visibility = Visibility.PUBLIC,
    val endDate: LocalDate? = null,
    val sessionCount: Int = 0,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>> = emptyMap(),
    val pendingRequestCount: Int = 0,
    val selectedTab: ManageTab = ManageTab.SCHEDULE,
    val schedule: List<ScheduleRow> = emptyList(),
    val confirmCancelDate: LocalDate? = null,
    val confirmation: ManageConfirmation? = null,
    val requests: List<RequestRow> = emptyList(),
    val requestsEmpty: Boolean = true,
    val roster: List<RosterRow> = emptyList(),
    val confirmRemove: ConfirmRemove? = null,
    val searchText: String = "",
    val match: InviteMatch? = null,
    val canSendInvite: Boolean = false,
    val noMatch: Boolean = false,
    val searchedId: String? = null,
    val optionsOpen: Boolean = false,
    val confirmCancelClass: Boolean = false,
    val error: UiError? = null,
    val navigation: Navigation? = null,
)
