package ph.project.active.presentation.dailyqr

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import ph.project.active.domain.repository.CoachKeyRepository
import ph.project.active.domain.usecase.GenerateDailyCode
import ph.project.active.data.repository.PersonRepository
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.data.model.SignedInAccount
import ph.project.active.presentation.attend.TodaySessionRow
import ph.project.active.presentation.common.watchConnection
import ph.project.active.domain.crypto.Ed25519KeyPair
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.requireCoach
import ph.project.active.presentation.common.toConnection
import ph.project.active.presentation.common.Navigation
import ph.project.active.domain.repository.sessionsOn
import ph.project.active.presentation.common.asUiError

class DailyQrViewModel(
    account: SignedInAccount,
    private val classes: ClassRepository,
    private val keys: CoachKeyRepository,
    private val people: PersonRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
    network: NetworkMonitor,
) : ViewModel() {
    init {
        requireCoach(account)
    }

    private val coachId = account.id
    private val fallbackName = account.name
    private val generate = GenerateDailyCode(keys)
    private val _state = MutableStateFlow(load(network.status.value.toConnection(), navigation = null))
    val state: StateFlow<DailyQrUiState> = _state.asStateFlow()

    init {
        watchConnection(network, _state) { copy(connection = it) }
    }

    fun refresh() {
        try {
            _state.value = load(_state.value.connection, _state.value.navigation)
        } catch (e: IllegalStateException) {
            _state.update { it.copy(error = e.asUiError()) }
        } catch (e: IllegalArgumentException) {
            _state.update { it.copy(error = e.asUiError()) }
        }
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
            error = null,
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
