package ph.project.active.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ph.project.active.data.NetworkMonitor
import ph.project.active.data.PersonRepository
import ph.project.active.data.SignedInAccount
import ph.project.active.domain.ClassRepository
import ph.project.active.domain.CoachKeyRepository
import ph.project.active.domain.Ed25519KeyPair
import ph.project.active.domain.GenerateDailyCode
import ph.project.active.domain.SessionStatus
import ph.project.active.domain.TimeSlot
import ph.project.active.domain.sessionsOn
import kotlin.time.Clock

data class TodaySessionRow(
    val className: String,
    val slot: TimeSlot,
    val status: SessionStatus,
)

data class DailyQrUiState(
    val today: LocalDate,
    val coachName: String,
    val validFor: LocalDate,
    val code: String,
    val todaysSessions: List<TodaySessionRow>,
    val classListUnavailable: Boolean,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)

class DailyQrViewModel(
    account: SignedInAccount,
    private val classes: ClassRepository,
    private val keys: CoachKeyRepository,
    private val people: PersonRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
    network: NetworkMonitor,
) : ViewModel() {
    private val coachId = account.id
    private val fallbackName = account.name
    private val generate = GenerateDailyCode(keys)
    private val _state = MutableStateFlow(load(network.status.value.toConnection(), navigation = null))
    val state: StateFlow<DailyQrUiState> = _state.asStateFlow()

    init {
        requireCoach(account)
        watchConnection(network, _state) { copy(connection = it) }
    }

    fun refresh() {
        _state.value = load(_state.value.connection, _state.value.navigation)
    }

    fun onNavigationHandled() {
        _state.value = _state.value.copy(navigation = null)
    }

    private fun load(connection: ConnectionUi, navigation: Navigation?): DailyQrUiState {
        ensureKey()
        val today = clock.todayIn(timeZone)
        val owned = classes.allClasses().filter { it.coachId == coachId }
        val sessions = owned.flatMap { trxClass ->
            classes.sessionsOn(trxClass.id, today).map { session ->
                TodaySessionRow(className = trxClass.name, slot = session.slot, status = session.status)
            }
        }.sortedWith(compareBy({ it.slot.start }, { it.className }))
        return DailyQrUiState(
            today = today,
            coachName = people.find(coachId)?.name ?: fallbackName,
            validFor = today,
            code = generate(coachId, today),
            todaysSessions = sessions,
            classListUnavailable = owned.isEmpty(),
            connection = connection,
            navigation = navigation,
        )
    }

    private fun ensureKey() {
        val existing = keys.find(coachId)
        if (existing == null || existing.privateKey.isEmpty()) {
            keys.save(coachId, Ed25519KeyPair.generate())
        }
    }
}
