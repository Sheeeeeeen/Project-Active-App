package ph.project.active.presentation.studentclasses

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
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.model.hasEnded
import ph.project.active.domain.model.nextSessionStart
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.domain.repository.activeClassIds
import ph.project.active.domain.repository.currentStatus
import ph.project.active.presentation.classdetail.ClassChip
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.requireStudent
import ph.project.active.presentation.common.toConnection
import ph.project.active.presentation.common.watchConnection

class StudentClassesViewModel(
    account: SignedInAccount,
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    private val people: PersonRepository,
    network: NetworkMonitor,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
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

    fun refresh() {
        publish()
    }

    fun onClassTapped(id: String) {
        _state.update { it.copy(navigation = Navigation.OpenClassDetail(id)) }
    }

    fun onNavigationHandled() {
        _state.update { it.copy(navigation = null) }
    }

    private fun publish() {
        val today = clock.todayIn(timeZone)
        val active = memberships.activeClassIds(studentId)
        val all = classes.allClasses().filter { !it.isCancelled }
        val myClasses = all
            .filter { it.id in active }
            .sortedWith(compareBy(nullsLast()) { it.nextSessionStart(today) })
            .map { it.toItem(ClassChip.Joined) }
        val publicClasses = all
            .filter { it.visibility == Visibility.PUBLIC && it.id !in active && !it.hasEnded(today) }
            .map { it.toItem(publicChip(it.id)) }
        _state.update { it.copy(myClasses = myClasses, publicClasses = publicClasses) }
    }

    private fun publicChip(classId: String): ClassChip? =
        when (memberships.currentStatus(classId, studentId)) {
            MembershipStatus.INVITED -> ClassChip.Invited
            MembershipStatus.PENDING_REQUEST -> ClassChip.Pending
            else -> null
        }

    private fun TrxClass.toItem(chip: ClassChip?): StudentClassItem = StudentClassItem(
        classId = id,
        name = name,
        chip = chip,
        coachName = people.find(coachId)?.name.orEmpty(),
        slotsByWeekday = slotsByWeekday,
    )
}
