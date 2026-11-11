package ph.project.active.presentation.guestclasses

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DayOfWeek
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.data.repository.PersonRepository
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.presentation.common.watchConnection
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.toConnection
import ph.project.active.presentation.common.Navigation
import ph.project.active.domain.model.Visibility

class GuestClassListViewModel(
    private val classes: ClassRepository,
    private val people: PersonRepository,
    private val network: NetworkMonitor,
) : ViewModel() {
    private val _state = MutableStateFlow(load(navigation = null, connection = network.status.value.toConnection()))
    val state: StateFlow<GuestClassListUiState> = _state.asStateFlow()

    init {
        watchConnection(network, _state) { copy(connection = it) }
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
        val items = classes.allClasses()
            .filter { it.visibility == Visibility.PUBLIC && !it.isCancelled }
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
