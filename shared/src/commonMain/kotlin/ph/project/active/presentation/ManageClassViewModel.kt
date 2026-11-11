package ph.project.active.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ph.project.active.data.NetworkMonitor
import ph.project.active.data.PersonRepository
import ph.project.active.data.SignedInAccount
import ph.project.active.domain.ApproveRequest
import ph.project.active.domain.AttendanceRepository
import ph.project.active.domain.CancelClass
import ph.project.active.domain.CancelDate
import ph.project.active.domain.ClassRepository
import ph.project.active.domain.DeclineRequest
import ph.project.active.domain.InviteByEmployeeId
import ph.project.active.domain.InviteResult
import ph.project.active.domain.MembershipRepository
import ph.project.active.domain.MembershipStatus
import ph.project.active.domain.RemoveFromRoster
import ph.project.active.domain.Role
import ph.project.active.domain.SessionStatus
import ph.project.active.domain.TimeSlot
import ph.project.active.domain.UserRepository
import ph.project.active.domain.Visibility
import ph.project.active.domain.activeRoster
import ph.project.active.domain.currentStatus
import kotlin.time.Clock

enum class ManageTab { SCHEDULE, REQUESTS, ROSTER, INVITE }

data class ScheduleRow(
    val sessionNumber: Int,
    val date: LocalDate,
    val slot: TimeSlot,
    val status: SessionStatus,
    val isToday: Boolean,
    val isPast: Boolean,
    val canCancelDate: Boolean,
)

data class RequestRow(val studentId: String, val name: String, val employeeId: String)

data class RosterRow(val studentId: String, val name: String, val employeeId: String)

data class ConfirmRemove(val studentId: String, val name: String)

data class InviteMatch(
    val name: String,
    val employeeId: String,
    val currentMembershipStatus: MembershipStatus?,
)

sealed interface ManageConfirmation {
    data class DateCancelled(val date: LocalDate) : ManageConfirmation
    data class InviteSent(val name: String) : ManageConfirmation
}

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

class ManageClassViewModel(
    account: SignedInAccount,
    private val classId: String,
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    @Suppress("unused") private val attendance: AttendanceRepository,
    private val people: PersonRepository,
    private val users: UserRepository,
    private val approveRequest: ApproveRequest,
    private val declineRequest: DeclineRequest,
    private val removeFromRoster: RemoveFromRoster,
    private val inviteByEmployeeId: InviteByEmployeeId,
    private val cancelDate: CancelDate,
    private val cancelClass: CancelClass,
    private val clock: Clock,
    private val timeZone: TimeZone,
    private val network: NetworkMonitor,
) : ViewModel() {
    private val _state: MutableStateFlow<ManageClassUiState>

    val state: StateFlow<ManageClassUiState>

    init {
        requireCoach(account)
        val trxClass = requireNotNull(classes.findClass(classId)) { "Unknown class $classId" }
        require(trxClass.coachId == account.id) { "Coach does not own $classId" }
        _state = MutableStateFlow(build(baseFlags(network.status.value.toConnection())))
        state = _state.asStateFlow()
        watchConnection(network, _state) { copy(connection = it) }
    }

    fun onSelectTab(tab: ManageTab) = _state.update { it.copy(selectedTab = tab) }

    fun onCancelDate(date: LocalDate) {
        if (offline()) return
        _state.update { it.copy(confirmCancelDate = date) }
    }

    fun onKeepDate() = _state.update { it.copy(confirmCancelDate = null) }

    fun onConfirmCancelDate() {
        if (offline()) return
        val date = _state.value.confirmCancelDate ?: return
        try {
            cancelDate(classId = classId, date = date)
            reload(confirmCancelDate = null, confirmation = ManageConfirmation.DateCancelled(date), error = null)
        } catch (e: IllegalArgumentException) {
            _state.update { it.copy(error = UiError.PastDate) }
        }
    }

    fun onApprove(studentId: String) {
        if (offline()) return
        approveRequest(classId = classId, studentId = studentId)
        reload(error = null)
    }

    fun onDecline(studentId: String) {
        if (offline()) return
        declineRequest(classId = classId, studentId = studentId)
        reload(error = null)
    }

    fun onRemove(studentId: String) {
        if (offline()) return
        val name = _state.value.roster.firstOrNull { it.studentId == studentId }?.name
            ?: people.find(studentId)?.name.orEmpty()
        _state.update { it.copy(confirmRemove = ConfirmRemove(studentId, name)) }
    }

    fun onKeep() = _state.update { it.copy(confirmRemove = null) }

    fun onConfirmRemove() {
        if (offline()) return
        val studentId = _state.value.confirmRemove?.studentId ?: return
        removeFromRoster(classId = classId, studentId = studentId)
        reload(confirmRemove = null, error = null)
    }

    fun onSearchTextChange(value: String) = _state.update { it.copy(searchText = value) }

    fun onSearch() {
        if (offline()) return
        val id = _state.value.searchText.trim()
        val student = users.findByEmployeeId(id)?.takeIf { it.role == Role.STUDENT }
        if (student == null) {
            _state.update { it.copy(noMatch = true, match = null, canSendInvite = false, searchedId = id, error = null) }
            return
        }
        val status = memberships.currentStatus(classId, student.id)
        _state.update {
            it.copy(
                match = InviteMatch(student.name, student.employeeId.orEmpty(), status),
                canSendInvite = status.canInvite(),
                noMatch = false,
                searchedId = id,
                error = null,
            )
        }
    }

    fun onSendInvite() {
        if (offline()) return
        val id = _state.value.searchText.trim()
        when (val result = inviteByEmployeeId(classId = classId, employeeId = id)) {
            is InviteResult.Invited -> {
                val name = people.find(result.membership.studentId)?.name ?: _state.value.match?.name.orEmpty()
                _state.update {
                    it.copy(
                        match = InviteMatch(name, id, MembershipStatus.INVITED),
                        canSendInvite = false,
                        noMatch = false,
                        confirmation = ManageConfirmation.InviteSent(name),
                        error = null,
                    )
                }
            }
            is InviteResult.AlreadyInvited -> setMatchStatus(MembershipStatus.INVITED)
            is InviteResult.AlreadyRequested -> setMatchStatus(MembershipStatus.PENDING_REQUEST)
            is InviteResult.AlreadyMember -> setMatchStatus(MembershipStatus.ACTIVE)
            InviteResult.NoMatch -> _state.update {
                it.copy(noMatch = true, match = null, canSendInvite = false, searchedId = id)
            }
            InviteResult.ClassCancelled, InviteResult.ClassNotFound -> _state.update { it.copy(error = UiError.ClassCancelled) }
        }
    }

    fun onOpenOptions() = _state.update { it.copy(optionsOpen = true) }

    fun onCancelWholeClass() = _state.update { it.copy(confirmCancelClass = true) }

    fun onKeepClass() = _state.update { it.copy(confirmCancelClass = false) }

    fun onConfirmCancelClass() {
        if (offline()) return
        cancelClass(classId)
        reload(confirmCancelClass = false, navigation = Navigation.Back, error = null)
    }

    fun onNavigationHandled() = _state.update { it.copy(navigation = null) }

    private fun offline(): Boolean {
        if (!network.isOffline()) return false
        _state.update { it.copy(error = UiError.Offline) }
        return true
    }

    private fun setMatchStatus(status: MembershipStatus) {
        val id = _state.value.searchText.trim()
        val current = _state.value.match
        val user = users.findByEmployeeId(id)
        _state.update {
            it.copy(
                match = InviteMatch(
                    name = current?.name ?: user?.name.orEmpty(),
                    employeeId = current?.employeeId ?: user?.employeeId ?: id,
                    currentMembershipStatus = status,
                ),
                canSendInvite = false,
                noMatch = false,
            )
        }
    }

    private fun reload(
        confirmCancelDate: LocalDate? = _state.value.confirmCancelDate,
        confirmation: ManageConfirmation? = _state.value.confirmation,
        confirmRemove: ConfirmRemove? = _state.value.confirmRemove,
        navigation: Navigation? = _state.value.navigation,
        error: UiError? = _state.value.error,
        confirmCancelClass: Boolean = _state.value.confirmCancelClass,
    ) {
        val current = _state.value
        _state.value = build(
            current.copy(
                confirmCancelDate = confirmCancelDate,
                confirmation = confirmation,
                confirmRemove = confirmRemove,
                navigation = navigation,
                error = error,
                confirmCancelClass = confirmCancelClass,
            ),
        )
    }

    private fun baseFlags(connection: ConnectionUi) = ManageClassUiState(
        name = "",
        visibility = Visibility.PUBLIC,
        endDate = null,
        sessionCount = 0,
        slotsByWeekday = emptyMap(),
        pendingRequestCount = 0,
        selectedTab = ManageTab.SCHEDULE,
        schedule = emptyList(),
        confirmCancelDate = null,
        confirmation = null,
        requests = emptyList(),
        requestsEmpty = true,
        roster = emptyList(),
        confirmRemove = null,
        searchText = "",
        match = null,
        canSendInvite = false,
        noMatch = false,
        searchedId = null,
        optionsOpen = false,
        confirmCancelClass = false,
        error = null,
        connection = connection,
        navigation = null,
    )

    private fun build(flags: ManageClassUiState): ManageClassUiState {
        val trxClass = classes.findClass(classId) ?: return flags
        val today = clock.todayIn(timeZone)
        val sessions = classes.sessionsOf(classId).sortedBy { it.sessionNumber }
        val schedule = sessions.map { session ->
            val isToday = session.date == today
            val isPast = session.date < today
            ScheduleRow(
                sessionNumber = session.sessionNumber,
                date = session.date,
                slot = session.slot,
                status = session.status,
                isToday = isToday,
                isPast = isPast,
                canCancelDate = session.status == SessionStatus.SCHEDULED && !isPast && !trxClass.isCancelled,
            )
        }
        val requests = memberships.forClass(classId)
            .map { it.studentId }
            .distinct()
            .filter { memberships.currentStatus(classId, it) == MembershipStatus.PENDING_REQUEST }
            .map { studentId -> personRow(studentId) { id, name, employeeId -> RequestRow(id, name, employeeId) } }
        val roster = memberships.activeRoster(classId).map { studentId ->
            personRow(studentId) { id, name, employeeId -> RosterRow(id, name, employeeId) }
        }
        return flags.copy(
            name = trxClass.name,
            visibility = trxClass.visibility,
            endDate = trxClass.endDate,
            sessionCount = sessions.size,
            slotsByWeekday = trxClass.slotsByWeekday,
            pendingRequestCount = requests.size,
            schedule = schedule,
            requests = requests,
            requestsEmpty = requests.isEmpty(),
            roster = roster,
        )
    }

    private fun <T> personRow(studentId: String, make: (String, String, String) -> T): T {
        val person = people.find(studentId)
        return make(studentId, person?.name.orEmpty(), person?.employeeId.orEmpty())
    }
}

private fun MembershipStatus?.canInvite(): Boolean =
    this == null || this == MembershipStatus.DECLINED || this == MembershipStatus.REMOVED
