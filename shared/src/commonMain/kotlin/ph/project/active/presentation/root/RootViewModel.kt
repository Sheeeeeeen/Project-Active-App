package ph.project.active.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ph.project.active.data.model.SignedInAccount
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.data.repository.SessionStore
import ph.project.active.presentation.common.toConnection
import ph.project.active.data.sync.OutboxSync
import ph.project.active.domain.usecase.transition
import ph.project.active.domain.model.Role

class RootViewModel(
    private val session: SessionStore,
    private val network: NetworkMonitor,
    private val outbox: OutboxSync,
) : ViewModel() {
    private val _state = MutableStateFlow(
        RootUiState(
            root = session.account.value.toRoot(),
            connection = network.status.value.toConnection(),
            navigation = null,
        ),
    )
    val state: StateFlow<RootUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Unconfined) {
            session.account.collect { account ->
                _state.update { it.copy(root = account.toRoot()) }
            }
        }
        viewModelScope.launch {
            var previous: NetworkStatus? = null
            network.status.collect { status ->
                _state.update { it.copy(connection = status.toConnection()) }
                if (status == NetworkStatus.ONLINE && previous != NetworkStatus.ONLINE) {
                    try {
                        outbox.sync()
                    } catch (_: Exception) {
                        // A failed upload leaves that row PENDING. The next transition tries again.
                    }
                }
                previous = status
            }
        }
    }

    fun onNavigationHandled() {
        _state.update { it.copy(navigation = null) }
    }

    private fun SignedInAccount?.toRoot(): Root = when (this?.role) {
        null -> Root.Guest
        Role.STUDENT -> Root.Student(this)
        Role.COACH -> Root.Coach(this)
    }
}
