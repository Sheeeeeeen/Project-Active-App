package ph.project.active.presentation.createclass

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.data.model.SignedInAccount
import ph.project.active.presentation.common.watchConnection
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.presentation.common.requireCoach
import ph.project.active.presentation.common.toConnection
import ph.project.active.domain.usecase.CreateClass
import ph.project.active.domain.model.Visibility
import ph.project.active.presentation.common.asUiError
import ph.project.active.presentation.common.isOffline
import ph.project.active.domain.model.TimeSlot
import ph.project.active.presentation.common.UiError
import ph.project.active.domain.repository.current

class CreateClassViewModel(
    account: SignedInAccount,
    private val classes: ClassRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
    private val network: NetworkMonitor,
) : ViewModel() {
    init {
        requireCoach(account)
    }

    private val coachId = account.id
    private val createClass = CreateClass(
        classes = classes,
        clock = clock,
        timeZone = timeZone,
        openEndedHorizon = DatePeriod(months = 12),
    )
    private val _state = MutableStateFlow(derive(blank()))
    val state: StateFlow<CreateClassUiState> = _state.asStateFlow()

    init {
        watchConnection(network, _state) { copy(connection = it) }
    }

    fun onNameChange(value: String) = edit { copy(name = value) }

    fun onVisibilityChange(value: Visibility) = edit { copy(visibility = value) }

    fun onEndDateChange(value: LocalDate?) = edit { copy(endDate = value) }

    fun onToggleDay(day: DayOfWeek) = edit {
        if (daysLocked) return@edit this
        if (day in selectedDays) {
            copy(selectedDays = selectedDays - day, slotsByWeekday = slotsByWeekday - day)
        } else {
            copy(selectedDays = selectedDays + day)
        }
    }

    fun onEveryDay() = edit {
        copy(selectedDays = DayOfWeek.entries.toSet(), daysLocked = true, repeat = RepeatMode.EVERY_DAY)
    }

    fun onSpecificDays() = edit { copy(daysLocked = false, repeat = RepeatMode.SPECIFIC_DAYS) }

    fun onAddSlot(day: DayOfWeek, start: LocalTime, end: LocalTime) = edit {
        if (start >= end) return@edit copy(slotError = SlotError.InvalidSlot)
        val slots = (slotsByWeekday[day].orEmpty() + TimeSlot(start, end)).sortedBy { it.start }
        copy(slotsByWeekday = slotsByWeekday + (day to slots), slotError = null)
    }

    fun onRemoveSlot(day: DayOfWeek, slot: TimeSlot) = edit {
        val slots = slotsByWeekday[day].orEmpty().filterNot { it == slot }
        copy(slotsByWeekday = slotsByWeekday + (day to slots))
    }

    fun onSave() {
        if (network.isOffline()) {
            _state.update { it.copy(error = UiError.Offline) }
            return
        }
        val current = _state.value
        if (!current.canSave) return
        val slots = current.selectedDays.associateWith { day ->
            current.slotsByWeekday[day].orEmpty().sortedBy { it.start }
        }.filterValues { it.isNotEmpty() }
        try {
            val saved = createClass(
                coachId = coachId,
                name = current.name,
                visibility = current.visibility,
                slotsByWeekday = slots,
                endDate = current.endDate,
            )
            _state.update { it.copy(savedClassId = saved.id, error = null) }
        } catch (e: IllegalArgumentException) {
            _state.update { it.copy(error = e.asUiError(), savedClassId = null) }
        } catch (e: IllegalStateException) {
            _state.update { it.copy(error = e.asUiError(), savedClassId = null) }
        }
    }

    fun onCancel() = Unit

    fun onNavigationHandled() {
        _state.update { it.copy(navigation = null) }
    }

    private inline fun edit(block: CreateClassUiState.() -> CreateClassUiState) {
        _state.update { current -> derive(current.block()) }
    }

    private fun blank(): CreateClassUiState = CreateClassUiState(
        name = "",
        repeat = RepeatMode.SPECIFIC_DAYS,
        selectedDays = emptySet(),
        visibility = Visibility.PUBLIC,
        endDate = null,
        daysLocked = false,
        slotsByWeekday = emptyMap(),
        slotError = null,
        canSave = false,
        previewDateCount = null,
        savedClassId = null,
        error = null,
        connection = network.status.value.toConnection(),
        navigation = null,
    )

    private fun derive(state: CreateClassUiState): CreateClassUiState {
        val canSave = state.name.trim().isNotEmpty() &&
            state.selectedDays.isNotEmpty() &&
            state.selectedDays.all { state.slotsByWeekday[it].orEmpty().isNotEmpty() }
        return state.copy(canSave = canSave, previewDateCount = preview(state))
    }

    private fun preview(state: CreateClassUiState): Int? {
        val end = state.endDate ?: return null
        val today = clock.todayIn(timeZone)
        var date = today
        var count = 0
        while (date <= end) {
            val hasSlot = date.dayOfWeek in state.selectedDays &&
                state.slotsByWeekday[date.dayOfWeek].orEmpty().isNotEmpty()
            if (hasSlot) count += 1
            date = date.plus(1, DateTimeUnit.DAY)
        }
        return count
    }
}
