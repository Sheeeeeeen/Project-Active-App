package ph.project.active.presentation.guestclasses

import androidx.lifecycle.ViewModel
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.data.repository.PersonRepository
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.model.hasEnded
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.DetailGraph
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.common.toConnection
import ph.project.active.presentation.common.watchConnection

class GuestClassListViewModel(
    private val classes: ClassRepository,
    private val people: PersonRepository,
    private val network: NetworkMonitor,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : ViewModel() {
    private val _state = MutableStateFlow(load(navigation = null, connection = network.status.value.toConnection()))
    val state: StateFlow<GuestClassListUiState> = _state.asStateFlow()

    init {
        DetailGraph.classes = classes
        DetailGraph.people = people
        DetailGraph.network = network
        watchConnection(network, _state) { copy(connection = it) }
    }

    fun refresh() {
        _state.value = load(navigation = _state.value.navigation, connection = _state.value.connection)
    }

    fun onRegisterOrSignIn() {
        _state.update { it.copy(navigation = Navigation.OpenRegistration(RegistrationOrigin.FromClassList)) }
    }

    fun onClassTapped(id: String) {
        _state.update { it.copy(navigation = Navigation.OpenClassDetail(id)) }
    }

    fun onNavigationHandled() {
        _state.update { it.copy(navigation = null) }
    }

    private fun load(navigation: Navigation?, connection: ConnectionUi): GuestClassListUiState {
        val today = clock.todayIn(timeZone)
        val items = classes.allClasses()
            .filter { trxClass ->
                trxClass.visibility == Visibility.PUBLIC && !trxClass.isCancelled && !trxClass.hasEnded(today)
            }
            .map { trxClass ->
                GuestClassItem(
                    classId = trxClass.id,
                    name = trxClass.name,
                    coachName = people.find(trxClass.coachId)?.name.orEmpty(),
                    slotsByWeekday = trxClass.slotsByWeekday,
                )
            }
        return GuestClassListUiState(
            items = items,
            isEmpty = items.isEmpty(),
            connection = connection,
            navigation = navigation,
        )
    }
}
