package ph.project.active.ui.manageclass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
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
import ph.project.active.ui.components.TrxCompactButton
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.ban
import projectactive.composeapp.generated.resources.calendar_check

@Composable
internal fun ScheduleTab(state: ManageClassUiState, onCancelDate: (LocalDate) -> Unit) {
    val colors = LocalTrxColors.current
    InfoCallout(UiCopy.cancelDateCalloutTitle(), UiCopy.cancelDateCalloutBody(), style = InfoCalloutStyle.Neutral)
    ManageSectionHeader(UiCopy.datesHeader(), UiCopy.dateCount(state.sessionCount), Modifier.padding(top = TrxSpacing.s16))
    TrxCard(Modifier.padding(top = TrxSpacing.s8), contentPadding = TrxSpacing.s0) {
        state.schedule.forEachIndexed { index, row ->
            if (index > 0) HorizontalDivider(color = colors.borderDefault)
            DateRow(row, onCancelDate)
        }
    }
}

@Composable
private fun DateRow(row: ScheduleRow, onCancelDate: (LocalDate) -> Unit) {
    val colors = LocalTrxColors.current
    val iso = row.date.toString()
    val cancelled = row.status == SessionStatus.CANCELLED
    val dimmed = row.isPast || cancelled
    Row(
        Modifier
            .testTag(ManageClassTestTags.dateRow(iso))
            .padding(horizontal = TrxSpacing.s16, vertical = TrxSpacing.s12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(UiCopy.sessionLabel(row.sessionNumber), color = colors.textTertiary, style = TrxTypography.overline)
                if (row.isToday) {
                    Text(
                        text = UiCopy.today(),
                        color = colors.textInverse,
                        style = TrxTypography.overline,
                        modifier = Modifier
                            .padding(start = TrxSpacing.s8)
                            .background(colors.surfaceInverse, CircleShape)
                            .padding(horizontal = TrxSpacing.s8),
                    )
                }
            }
            Text(
                text = formatDate(row.date),
                color = if (dimmed && !cancelled) colors.textTertiary else colors.textPrimary,
                style = TrxTypography.headline,
                textDecoration = if (cancelled) TextDecoration.LineThrough else null,
            )
            Text(
                text = formatSlot(row.slot),
                color = if (dimmed) colors.textTertiary else colors.textSecondary,
                style = TrxTypography.subhead,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            if (cancelled) {
                StatusChip(UiCopy.cancelled(), Res.drawable.ban, colors.surfaceAlt, colors.textSecondary)
            } else {
                StatusChip(UiCopy.scheduled(), Res.drawable.calendar_check, colors.infoBg, colors.infoFg)
            }
            if (row.isPast && !cancelled) {
                Text(
                    text = UiCopy.pastDate(),
                    color = colors.textTertiary,
                    style = TrxTypography.footnote,
                    modifier = Modifier.padding(top = TrxSpacing.s8),
                )
            }
            if (row.canCancelDate) {
                TrxCompactButton(
                    label = UiCopy.cancelDate(),
                    kind = TrxButtonKind.Secondary,
                    tag = ManageClassTestTags.cancelDate(iso),
                    onClick = { onCancelDate(row.date) },
                )
            }
        }
    }
}
