package ph.project.active.ui.attend

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ph.project.active.presentation.attend.AttendChoice
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.common.formatDate
import ph.project.active.ui.components.InfoCallout
import ph.project.active.ui.components.InfoCalloutStyle
import ph.project.active.ui.components.TrxButtonKind
import ph.project.active.ui.components.TrxLargeButton
import ph.project.active.ui.platform.CameraAccess
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.check
import projectactive.composeapp.generated.resources.triangle_alert
import projectactive.composeapp.generated.resources.x
import kotlinx.datetime.LocalDate

@Composable
fun ClassSheet(
    today: LocalDate,
    choices: List<AttendChoice>,
    selectedSessionId: String?,
    canScan: Boolean,
    cameraAccess: CameraAccess,
    onChoose: (String) -> Unit,
    onClose: () -> Unit,
    onScan: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTrxColors.current
    val denied = cameraAccess == CameraAccess.DENIED_FOR_GOOD && selectedSessionId != null
    val shape = RoundedCornerShape(topStart = TrxSpacing.radiusLg, topEnd = TrxSpacing.radiusLg)
    BoxWithConstraints(modifier.fillMaxWidth()) {
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight * 0.9f)
            .clip(shape)
            .background(colors.surface)
            .testTag(AttendTestTags.sheet)
            .padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = UiCopy.chooseClassTitle(),
                color = colors.textPrimary,
                style = TrxTypography.sectionTitle,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onClose, modifier = Modifier.size(TrxSpacing.touchTarget).testTag(AttendTestTags.sheetClose)) {
                Icon(
                    painter = painterResource(Res.drawable.x),
                    contentDescription = UiCopy.close(),
                    tint = colors.textPrimary,
                )
            }
        }
        Text(
            text = UiCopy.chooseClassSubtitle(today),
            color = colors.textSecondary,
            style = TrxTypography.subhead,
            modifier = Modifier.padding(bottom = TrxSpacing.s16),
        )
        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
        if (choices.isEmpty()) {
            Column(Modifier.testTag(AttendTestTags.sheetEmpty).padding(vertical = TrxSpacing.s16)) {
                Text(UiCopy.noClassesToday(), color = colors.textPrimary, style = TrxTypography.headline)
                Text(
                    text = UiCopy.noClassesTodayBody(),
                    color = colors.textSecondary,
                    style = TrxTypography.subhead,
                    modifier = Modifier.padding(top = TrxSpacing.s4),
                )
            }
        } else {
            choices.forEach { choice ->
                ChoiceRow(
                    choice = choice,
                    selected = choice.sessionId == selectedSessionId,
                    onChoose = { onChoose(choice.sessionId) },
                )
            }
        }
        }
        if (denied) {
            InfoCallout(
                title = UiCopy.cameraOffTitle(),
                body = UiCopy.cameraOffBody(),
                tag = AttendTestTags.cameraDenied,
                icon = Res.drawable.triangle_alert,
                style = InfoCalloutStyle.Warning,
                modifier = Modifier.padding(top = TrxSpacing.s16),
            )
            TrxLargeButton(
                label = UiCopy.openSettings(),
                kind = TrxButtonKind.Primary,
                tag = AttendTestTags.cameraOpenSettings,
                onClick = onOpenSettings,
                enabled = true,
                modifier = Modifier.padding(top = TrxSpacing.s16),
            )
        } else {
            TrxLargeButton(
                label = UiCopy.scanCoachCode(),
                kind = TrxButtonKind.Primary,
                tag = AttendTestTags.sheetScan,
                onClick = onScan,
                enabled = canScan,
                modifier = Modifier.padding(top = TrxSpacing.s16),
            )
            if (choices.isNotEmpty()) {
                Text(
                    text = UiCopy.choiceFootnote(),
                    color = colors.textTertiary,
                    style = TrxTypography.footnote,
                    modifier = Modifier.padding(top = TrxSpacing.s12),
                )
            }
        }
    }
    }
}

@Composable
private fun ChoiceRow(choice: AttendChoice, selected: Boolean, onChoose: () -> Unit) {
    val colors = LocalTrxColors.current
    val shape = RoundedCornerShape(TrxSpacing.radiusMd)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = TrxSpacing.s8)
            .clip(shape)
            .background(if (selected) colors.accentSubtle else colors.surfaceAlt)
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.textPrimary else colors.borderDefault, shape)
            .selectable(selected = selected, onClick = onChoose, role = Role.RadioButton)
            .testTag(AttendTestTags.choice(choice.sessionId))
            .padding(TrxSpacing.s16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(choice.className, color = colors.textPrimary, style = TrxTypography.headline)
            Text(
                text = UiCopy.choiceLine(choice.slot, choice.coachName),
                color = colors.textSecondary,
                style = TrxTypography.subhead,
                modifier = Modifier.padding(top = TrxSpacing.s4),
            )
        }
        if (selected) {
            Icon(
                painter = painterResource(Res.drawable.check),
                contentDescription = null,
                tint = colors.canvas,
                modifier = Modifier
                    .padding(start = TrxSpacing.s12)
                    .size(TrxSpacing.iconMedium)
                    .background(colors.textPrimary, CircleShape)
                    .padding(TrxSpacing.s4),
            )
        } else {
            Box(
                Modifier
                    .padding(start = TrxSpacing.s12)
                    .size(TrxSpacing.iconMedium)
                    .border(2.dp, colors.borderStrong, CircleShape),
            )
        }
    }
}
