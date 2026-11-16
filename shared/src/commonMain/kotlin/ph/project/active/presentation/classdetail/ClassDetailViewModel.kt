package ph.project.active.presentation.classdetail

import androidx.lifecycle.ViewModel
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ph.project.active.data.model.SignedInAccount
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.data.repository.PersonRepository
import ph.project.active.domain.model.JoinResult
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.Role
import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.model.hasEnded
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.domain.repository.currentStatus
import ph.project.active.domain.usecase.RequestToJoin
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.common.UiError
import ph.project.active.presentation.common.isOffline
import ph.project.active.presentation.common.toConnection
import ph.project.active.presentation.common.watchConnection
import ph.project.active.presentation.manageclass.RequestAction

class ClassDetailViewModel(
    private val classId: String,
    private val viewer: SignedInAccount?,
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    private val people: PersonRepository,
    private val requestToJoin: RequestToJoin,
    private val network: NetworkMonitor,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    joinOutcome: JoinOutcome? = null,
) : ViewModel() {
    private var sending = false
    private var notice: JoinOutcome? = joinOutcome
    private val _state = MutableStateFlow(
        load(error = null, navigation = null, connection = network.status.value.toConnection()),
    )
    val state: StateFlow<ClassDetailUiState> = _state.asStateFlow()

    init {
        watchConnection(network, _state) { copy(connection = it) }
    }

    fun onRequestToJoin() {
        val trxClass = classes.findClass(classId)
        if (trxClass != null && trxClass.hasEnded(today()) && !trxClass.isCancelled) return
        notice = null
        val account = viewer
        if (account == null) {
            _state.update {
                it.copy(
                    error = null,
                    notice = null,
                    navigation = Navigation.OpenRegistration(RegistrationOrigin.FromRequestToJoin(classId)),
                )
            }
            return
        }
        if (account.role != Role.STUDENT) return
        if (_state.value.requestAction is RequestAction.Pending || sending) return
        if (network.isOffline()) {
            _state.update { it.copy(error = UiError.Offline, notice = null) }
            return
        }
        sending = true
        _state.update { it.copy(isSending = true, error = null, notice = null) }
        try {
            val error = when (requestToJoin(classId = classId, studentId = account.id)) {
                JoinResult.ClassCancelled -> UiError.ClassCancelled
                JoinResult.PrivateClass -> UiError.PrivateClass
                else -> null
            }
            _state.value = load(
                error = error,
                navigation = _state.value.navigation,
                connection = _state.value.connection,
            )
        } finally {
            sending = false
            if (_state.value.isSending) _state.update { it.copy(isSending = false) }
        }
    }

    fun refresh() {
        notice = null
        _state.value = load(
            error = null,
            navigation = _state.value.navigation,
            connection = _state.value.connection,
        )
    }

    fun onNavigationHandled() {
        _state.update { it.copy(navigation = null) }
    }

    private fun today() = clock.todayIn(timeZone)

    private fun load(error: UiError?, navigation: Navigation?, connection: ConnectionUi): ClassDetailUiState {
        val trxClass = classes.findClass(classId)
        val visible = trxClass != null && canSee(trxClass, viewer)
        val today = today()
        val content = if (trxClass != null && visible) {
            ClassContent(
                name = trxClass.name,
                visibility = trxClass.visibility,
                coachName = people.find(trxClass.coachId)?.name.orEmpty(),
                slotsByWeekday = trxClass.slotsByWeekday,
                endDate = trxClass.endDate,
                isCancelled = trxClass.isCancelled,
                isEnded = trxClass.hasEnded(today),
            )
        } else {
            null
        }
        return ClassDetailUiState(
            content = content,
            unavailable = !visible,
            requestAction = requestAction(trxClass, today),
            requestAgain = requestAgain(trxClass, today),
            error = error,
            connection = connection,
            navigation = navigation,
            notice = notice,
        )
    }

    private fun canSee(trxClass: TrxClass, viewer: SignedInAccount?): Boolean {
        if (trxClass.visibility == Visibility.PUBLIC) return true
        if (viewer == null) return false
        if (trxClass.coachId == viewer.id) return true
        return memberships.currentStatus(trxClass.id, viewer.id) == MembershipStatus.ACTIVE
    }

    private fun requestAction(trxClass: TrxClass?, today: kotlinx.datetime.LocalDate): RequestAction {
        if (trxClass != null && (trxClass.isCancelled || trxClass.hasEnded(today))) return RequestAction.None
        val account = viewer ?: return RequestAction.RequestToJoin(opensRegistration = true)
        if (account.role == Role.COACH) return RequestAction.None
        return when (trxClass?.let { memberships.currentStatus(it.id, account.id) }) {
            MembershipStatus.PENDING_REQUEST -> RequestAction.Pending
            MembershipStatus.ACTIVE -> RequestAction.Joined
            MembershipStatus.INVITED -> RequestAction.Invited
            MembershipStatus.DECLINED, MembershipStatus.REMOVED, null ->
                RequestAction.RequestToJoin(opensRegistration = false)
        }
    }

    private fun requestAgain(trxClass: TrxClass?, today: kotlinx.datetime.LocalDate): Boolean {
        if (trxClass != null && (trxClass.isCancelled || trxClass.hasEnded(today))) return false
        val account = viewer ?: return false
        if (account.role != Role.STUDENT || trxClass == null) return false
        val status = memberships.currentStatus(trxClass.id, account.id)
        return status == MembershipStatus.DECLINED || status == MembershipStatus.REMOVED
    }
}
