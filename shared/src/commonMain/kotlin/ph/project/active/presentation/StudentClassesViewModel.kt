package ph.project.active.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ph.project.active.data.NetworkMonitor
import ph.project.active.data.PersonRepository
import ph.project.active.data.SignedInAccount
import ph.project.active.domain.ClassRepository
import ph.project.active.domain.MembershipRepository
import ph.project.active.domain.MembershipStatus
import ph.project.active.domain.Visibility
import ph.project.active.domain.activeClassIds
import ph.project.active.domain.currentStatus

enum class ClassChip { Joined, Invited, Pending }

data class StudentClassItem(
    val classId: String,
    val name: String,
    val chip: ClassChip?,
)

data class StudentClassesUiState(
    val studentName: String,
    val employeeId: String?,
    val myClasses: List<StudentClassItem>,
    val publicClasses: List<StudentClassItem>,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)

class StudentClassesViewModel(
    account: SignedInAccount,
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    private val people: PersonRepository,
    network: NetworkMonitor,
) : ViewModel() {
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
        requireStudent(account)
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

    @Suppress("unused")
    private val peopleRef = people
}
