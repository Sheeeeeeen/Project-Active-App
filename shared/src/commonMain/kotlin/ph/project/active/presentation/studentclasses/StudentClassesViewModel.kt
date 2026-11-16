package ph.project.active.presentation.studentclasses

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.data.repository.PersonRepository
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.data.model.SignedInAccount
import ph.project.active.presentation.common.watchConnection
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.domain.repository.activeClassIds
import ph.project.active.presentation.common.requireStudent
import ph.project.active.domain.repository.currentStatus
import ph.project.active.presentation.common.toConnection
import ph.project.active.presentation.common.Navigation
import ph.project.active.domain.model.Visibility
import ph.project.active.presentation.classdetail.ClassChip

class StudentClassesViewModel(
    account: SignedInAccount,
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    @Suppress("unused") private val people: PersonRepository,
    network: NetworkMonitor,
) : ViewModel() {
    init {
        requireStudent(account)
    }

    private val studentId = account.id
    private val _state = MutableStateFlow(
        StudentClassesUiState(
            studentName = account.name,
            employeeId = account.employeeId,
            myClasses = emptyList(),
            publicClasses = emptyList(),
            connection = network.status.value.toConnection(),
            navigation = null,
        ),
    )
    val state: StateFlow<StudentClassesUiState> = _state.asStateFlow()

    init {
        publish()
        watchConnection(network, _state) { copy(connection = it) }
    }

    fun onClassTapped(id: String) {
        _state.update { it.copy(navigation = Navigation.OpenClassDetail(id)) }
    }

    fun onNavigationHandled() {
        _state.update { it.copy(navigation = null) }
    }

    private fun publish() {
        val active = memberships.activeClassIds(studentId)
        val myClasses = classes.allClasses()
            .filter { it.id in active && !it.isCancelled }
            .map { StudentClassItem(it.id, it.name, ClassChip.Joined) }
        val publicClasses = classes.allClasses()
            .filter { it.visibility == Visibility.PUBLIC && !it.isCancelled && it.id !in active }
            .map { trxClass ->
                val chip = when (memberships.currentStatus(trxClass.id, studentId)) {
                    MembershipStatus.INVITED -> ClassChip.Invited
                    MembershipStatus.PENDING_REQUEST -> ClassChip.Pending
                    else -> null
                }
                StudentClassItem(trxClass.id, trxClass.name, chip)
            }
        _state.update { it.copy(myClasses = myClasses, publicClasses = publicClasses) }
    }
}
