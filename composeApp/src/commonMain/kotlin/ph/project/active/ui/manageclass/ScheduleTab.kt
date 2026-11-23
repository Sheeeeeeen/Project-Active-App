package ph.project.active.ui.manageclass

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.SessionStatus
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.common.formatDate
import ph.project.active.presentation.common.formatSlot
import ph.project.active.presentation.createclass.ScheduleRow
import ph.project.active.presentation.manageclass.ManageClassUiState
import ph.project.active.ui.components.InfoCallout
import ph.project.active.ui.components.InfoCalloutStyle
import ph.project.active.ui.components.StatusChip
import ph.project.active.ui.components.TrxButtonKind
import ph.project.active.ui.components.TrxCard
import ph.project.active.ui.components.TrxLargeButton
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.ban
import projectactive.composeapp.generated.resources.calendar_check

@Composable
internal fun ScheduleTab(state: ManageClassUiState, onCancelDate: (LocalDate) -> Unit) {
    InfoCallout(UiCopy.cancelDateCalloutTitle(), UiCopy.cancelDateCalloutBody(), style = InfoCalloutStyle.Neutral)
    ManageSectionHeader(UiCopy.datesHeader(), UiCopy.dateCount(state.sessionCount), Modifier.padding(top = TrxSpacing.s16))
    state.schedule.forEach { row -> DateRow(row, onCancelDate) }
}

@Composable
private fun DateRow(row: ScheduleRow, onCancelDate: (LocalDate) -> Unit) {
    val colors = LocalTrxColors.current
    val iso = row.date.toString()
    TrxCard(
        Modifier
            .padding(top = TrxSpacing.s12)
            .testTag(ManageClassTestTags.dateRow(iso)),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(UiCopy.sessionLabel(row.sessionNumber), color = colors.textTertiary, style = TrxTypography.overline)
                    if (row.isToday) {
                        Text(
                            text = UiCopy.today(),
                            color = colors.accentText,
                            style = TrxTypography.overline,
                            modifier = Modifier.padding(start = TrxSpacing.s8),
                        )
                    }
                }
                Text(formatDate(row.date), color = colors.textPrimary, style = TrxTypography.headline)
                Text(formatSlot(row.slot), color = colors.textSecondary, style = TrxTypography.subhead)
                if (row.isPast && row.status == SessionStatus.SCHEDULED) {
                    Text(UiCopy.pastDate(), color = colors.textTertiary, style = TrxTypography.footnote)
                }
            }
            if (row.status == SessionStatus.CANCELLED) {
                StatusChip(UiCopy.cancelled(), Res.drawable.ban, colors.surfaceAlt, colors.textSecondary)
            } else {
                StatusChip(UiCopy.scheduled(), Res.drawable.calendar_check, colors.successBg, colors.successFg)
            }
        }
        if (row.canCancelDate) {
            TrxLargeButton(
                label = UiCopy.cancelDate(),
                kind = TrxButtonKind.Secondary,
                tag = ManageClassTestTags.cancelDate(iso),
                onClick = { onCancelDate(row.date) },
                enabled = true,
                modifier = Modifier.padding(top = TrxSpacing.s12),
                height = TrxSpacing.touchTarget,
                textStyle = TrxTypography.subheadStrong,
            )
        }
    }
}
