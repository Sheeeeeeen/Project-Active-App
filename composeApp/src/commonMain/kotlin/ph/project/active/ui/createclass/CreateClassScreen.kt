package ph.project.active.ui.createclass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.painterResource
import ph.project.active.domain.model.TimeSlot
import ph.project.active.domain.model.Visibility
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.common.UiError
import ph.project.active.presentation.common.formatDate
import ph.project.active.presentation.common.formatDaysLong
import ph.project.active.presentation.common.formatSlot
import ph.project.active.presentation.createclass.CreateClassUiState
import ph.project.active.presentation.createclass.RepeatMode
import ph.project.active.ui.components.FormError
import ph.project.active.ui.components.InfoCallout
import ph.project.active.ui.components.OfflineBanner
import ph.project.active.ui.components.TrxButtonKind
import ph.project.active.ui.components.TrxLargeButton
import ph.project.active.ui.components.TrxTextField
import ph.project.active.ui.platform.LightStatusBarIcons
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.calendar_days
import projectactive.composeapp.generated.resources.x

object CreateClassTestTags {
    const val title = "create_class_title"
    const val cancel = "create_class_cancel"
    const val name = "create_class_name"
    const val nameError = "create_class_name_error"
    const val nameErrorIcon = "create_class_name_error_icon"
    const val save = "create_class_save"
    const val preview = "create_class_preview"
    const val formError = "create_class_form_error"
}

/** D3–D6: a full-screen route that looks like the Figma sheet. Save and Cancel both return to D1. */
@Composable
fun CreateClassScreen(
    state: CreateClassUiState,
    onNameChange: (String) -> Unit,
    onSpecificDays: () -> Unit,
    onEveryDay: () -> Unit,
    onToggleDay: (DayOfWeek) -> Unit,
    onAddSlot: (DayOfWeek, LocalTime, LocalTime) -> Unit,
    onRemoveSlot: (DayOfWeek, TimeSlot) -> Unit,
    onVisibilityChange: (Visibility) -> Unit,
    onEndDateChange: (LocalDate?) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons(lightIcons = false)
    val colors = LocalTrxColors.current
    LaunchedEffect(state.savedClassId) {
        if (state.savedClassId != null) onSaved()
    }
    var nameTouched by rememberSaveable { mutableStateOf(false) }
    var addingSlotFor by rememberSaveable { mutableStateOf<DayOfWeek?>(null) }
    var wantsEndDate by rememberSaveable { mutableStateOf(false) }
    var pickingEndDate by rememberSaveable { mutableStateOf(false) }
    val noEndDate = !wantsEndDate && state.endDate == null
    Scaffold(
        modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        containerColor = colors.canvas,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom),
        topBar = { TopBar(onCancel) },
        bottomBar = {
            TrxLargeButton(
                label = UiCopy.saveClass(),
                kind = TrxButtonKind.Primary,
                tag = CreateClassTestTags.save,
                onClick = onSave,
                enabled = state.canSave,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    .padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16),
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            state.connection.banner?.let { banner -> OfflineBanner(banner) }
            Column(
                Modifier.padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16),
                verticalArrangement = Arrangement.spacedBy(TrxSpacing.s16),
            ) {
                state.error?.let { error -> FormError(UiCopy.uiError(error), CreateClassTestTags.formError) }
                TrxTextField(
                    value = state.name,
                    onValueChange = {
                        nameTouched = true
                        onNameChange(it)
                    },
                    label = UiCopy.classNameLabel(),
                    placeholder = UiCopy.classNamePlaceholder(),
                    fieldTag = CreateClassTestTags.name,
                    enabled = true,
                    keyboardType = KeyboardType.Text,
                    errorMessage = if (nameTouched && state.name.isBlank()) UiCopy.requiredField() else null,
                    errorTag = CreateClassTestTags.nameError,
                    errorIconTag = CreateClassTestTags.nameErrorIcon,
                )
                SectionLabel(UiCopy.repeatsLabel())
                Segments(
                    first = UiCopy.specificDays(),
                    second = UiCopy.everyDay(),
                    firstSelected = state.repeat == RepeatMode.SPECIFIC_DAYS,
                    onFirst = onSpecificDays,
                    onSecond = onEveryDay,
                )
                DayChips(state, onToggleDay)
                SectionLabel(UiCopy.timeSlotsLabel())
                if (state.selectedDays.isEmpty()) {
                    Helper(UiCopy.chooseDayFirst())
                } else {
                    state.selectedDays.sortedBy { it.ordinal }.forEach { day ->
                        DaySlots(
                            day = day,
                            state = state,
                            onAdd = { addingSlotFor = day },
                            onRemove = { slot -> onRemoveSlot(day, slot) },
                        )
                    }
                }
                state.slotError?.let { Text(UiCopy.uiError(UiError.InvalidSlot), color = colors.dangerFg, style = TrxTypography.label) }
                SectionLabel(UiCopy.visibilityLabel())
                Segments(
                    first = UiCopy.visibilityPublic(),
                    second = UiCopy.visibilityPrivate(),
                    firstSelected = state.visibility == Visibility.PUBLIC,
                    onFirst = { onVisibilityChange(Visibility.PUBLIC) },
                    onSecond = { onVisibilityChange(Visibility.PRIVATE) },
                )
                Helper(if (state.visibility == Visibility.PUBLIC) UiCopy.publicHelper() else UiCopy.privateHelper())
                SectionLabel(UiCopy.endsLabel())
                NoEndDateToggle(
                    checked = noEndDate,
                    onChange = { on ->
                        wantsEndDate = !on
                        if (on) onEndDateChange(null)
                    },
                )
                if (!noEndDate) {
                    EndDateRow(state.endDate, onChoose = { pickingEndDate = true })
                }
                Preview(state)
            }
        }
    }
    addingSlotFor?.let { day ->
        SlotDialog(
            onAdd = { start, end ->
                onAddSlot(day, start, end)
                addingSlotFor = null
            },
            onDismiss = { addingSlotFor = null },
        )
    }
    if (pickingEndDate) {
        EndDateDialog(
            onPick = { date ->
                onEndDateChange(date)
                pickingEndDate = false
            },
            onDismiss = { pickingEndDate = false },
        )
    }
}

@Composable
private fun TopBar(onCancel: () -> Unit) {
    val colors = LocalTrxColors.current
    Box(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(horizontal = TrxSpacing.s8, vertical = TrxSpacing.s8),
    ) {
        TextButton(
            onClick = onCancel,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .testTag(CreateClassTestTags.cancel),
        ) {
            Text(UiCopy.cancel(), color = colors.accentText, style = TrxTypography.body)
        }
        Text(
            text = UiCopy.newClassTitle(),
            color = colors.textPrimary,
            style = TrxTypography.headline,
            modifier = Modifier
                .align(Alignment.Center)
                .testTag(CreateClassTestTags.title),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = LocalTrxColors.current.textPrimary, style = TrxTypography.subheadStrong)
}

@Composable
private fun Helper(text: String) {
    Text(text, color = LocalTrxColors.current.textSecondary, style = TrxTypography.footnote)
}

@Composable
private fun Segments(
    first: String,
    second: String,
    firstSelected: Boolean,
    onFirst: () -> Unit,
    onSecond: () -> Unit,
) {
    val colors = LocalTrxColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .background(colors.surfaceAlt, RoundedCornerShape(TrxSpacing.radiusMd))
            .padding(TrxSpacing.s4),
    ) {
        Segment(first, firstSelected, onFirst, Modifier.weight(1f))
        Segment(second, !firstSelected, onSecond, Modifier.weight(1f))
    }
}

@Composable
private fun Segment(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val colors = LocalTrxColors.current
    Box(
        modifier
            .heightIn(min = TrxSpacing.touchTarget)
            .clip(RoundedCornerShape(TrxSpacing.radiusSm))
            .background(if (selected) colors.surface else colors.surfaceAlt)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) colors.textPrimary else colors.textSecondary,
            style = TrxTypography.subheadStrong,
        )
    }
}

@Composable
private fun DayChips(state: CreateClassUiState, onToggleDay: (DayOfWeek) -> Unit) {
    val colors = LocalTrxColors.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TrxSpacing.s4)) {
        DayOfWeek.entries.forEach { day ->
            val selected = day in state.selectedDays
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = TrxSpacing.touchTarget)
                    .clip(RoundedCornerShape(TrxSpacing.radiusSm))
                    .background(if (selected) colors.accentDefault else colors.surface)
                    .border(1.dp, if (selected) colors.accentDefault else colors.borderDefault, RoundedCornerShape(TrxSpacing.radiusSm))
                    .selectable(
                        selected = selected,
                        enabled = !state.daysLocked,
                        role = Role.Checkbox,
                        onClick = { onToggleDay(day) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = day.shortName(),
                    color = if (selected) colors.textOnAccent else colors.textPrimary,
                    style = TrxTypography.label,
                )
            }
        }
    }
}

@Composable
private fun DaySlots(
    day: DayOfWeek,
    state: CreateClassUiState,
    onAdd: () -> Unit,
    onRemove: (TimeSlot) -> Unit,
) {
    val colors = LocalTrxColors.current
    val slots = state.slotsByWeekday[day].orEmpty()
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(TrxSpacing.radiusMd))
            .border(1.dp, colors.borderDefault, RoundedCornerShape(TrxSpacing.radiusMd))
            .padding(TrxSpacing.s12),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(formatDaysLong(setOf(day)), color = colors.textPrimary, style = TrxTypography.subheadStrong, modifier = Modifier.weight(1f))
            Text(UiCopy.slotCount(slots.size), color = colors.textTertiary, style = TrxTypography.footnote)
        }
        slots.forEach { slot ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatSlot(slot), color = colors.textSecondary, style = TrxTypography.subhead, modifier = Modifier.weight(1f))
                IconButton(onClick = { onRemove(slot) }) {
                    Icon(
                        painter = painterResource(Res.drawable.x),
                        contentDescription = UiCopy.remove(),
                        tint = colors.textSecondary,
                        modifier = Modifier.size(TrxSpacing.iconSmall),
                    )
                }
            }
        }
        TextButton(onClick = onAdd) {
            Text(UiCopy.addTimeSlot(), color = colors.accentText, style = TrxTypography.subheadStrong)
        }
    }
}

@Composable
private fun NoEndDateToggle(checked: Boolean, onChange: (Boolean) -> Unit) {
    val colors = LocalTrxColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = TrxSpacing.touchTarget)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(UiCopy.noEndDate(), color = colors.textPrimary, style = TrxTypography.body, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun EndDateRow(endDate: LocalDate?, onChoose: () -> Unit) {
    val colors = LocalTrxColors.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(UiCopy.endDateLabel(), color = colors.textPrimary, style = TrxTypography.body, modifier = Modifier.weight(1f))
        TextButton(onClick = onChoose) {
            Icon(
                painter = painterResource(Res.drawable.calendar_days),
                contentDescription = null,
                tint = colors.accentText,
                modifier = Modifier
                    .padding(end = TrxSpacing.s8)
                    .size(TrxSpacing.iconSmall),
            )
            Text(endDate?.let { formatDate(it) } ?: UiCopy.chooseDate(), color = colors.accentText, style = TrxTypography.body)
        }
    }
}

@Composable
private fun Preview(state: CreateClassUiState) {
    if (!state.canSave) return
    val count = state.previewDateCount
    val today = state.today
    val first = state.previewFirstDate
    val last = state.previewLastDate
    val days = formatDaysLong(state.selectedDays.filter { state.slotsByWeekday[it].orEmpty().isNotEmpty() }.toSet())
    if (state.endDate == null) {
        InfoCallout(UiCopy.noEndDate(), UiCopy.noEndDateBody(), tag = CreateClassTestTags.preview)
    } else if (count != null && today != null && first != null && last != null) {
        InfoCallout(UiCopy.previewTitle(count), UiCopy.previewBody(today, days, first, last), tag = CreateClassTestTags.preview)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SlotDialog(onAdd: (LocalTime, LocalTime) -> Unit, onDismiss: () -> Unit) {
    val start = rememberTimePickerState(initialHour = 17, initialMinute = 30)
    val end = rememberTimePickerState(initialHour = 18, initialMinute = 30)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(UiCopy.addTimeSlot()) },
        text = {
            Column {
                Text(UiCopy.startLabel(), style = TrxTypography.label)
                TimeInput(start)
                Text(UiCopy.endLabel(), style = TrxTypography.label)
                TimeInput(end)
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(LocalTime(start.hour, start.minute), LocalTime(end.hour, end.minute)) }) {
                Text(UiCopy.addTimeSlot())
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiCopy.cancel()) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EndDateDialog(onPick: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val picker = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                picker.selectedDateMillis?.let { millis -> onPick(LocalDate.fromEpochDays((millis / MILLIS_PER_DAY).toInt())) }
            }) {
                Text(UiCopy.chooseDate())
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiCopy.cancel()) } },
    ) {
        DatePicker(picker)
    }
}

private fun DayOfWeek.shortName(): String = formatDaysLong(setOf(this)).take(3)

private const val MILLIS_PER_DAY = 86_400_000L
