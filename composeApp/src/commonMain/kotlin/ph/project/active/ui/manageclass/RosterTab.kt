package ph.project.active.ui.manageclass

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.manageclass.ManageClassUiState
import ph.project.active.presentation.manageclass.RosterRow
import ph.project.active.ui.components.TrxButtonKind
import ph.project.active.ui.components.TrxCard
import ph.project.active.ui.components.TrxLargeButton
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

@Composable
internal fun RosterTab(state: ManageClassUiState, onRemove: (String) -> Unit) {
    ManageSectionHeader(UiCopy.onTheRoster(), UiCopy.studentCount(state.roster.size))
    state.roster.forEach { row -> RosterCard(row, onRemove) }
    ManageFootnote(UiCopy.rosterFootnote())
}

@Composable
private fun RosterCard(row: RosterRow, onRemove: (String) -> Unit) {
    val colors = LocalTrxColors.current
    TrxCard(Modifier.padding(top = TrxSpacing.s12)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(row.name, color = colors.textPrimary, style = TrxTypography.headline)
                Text(row.employeeId, color = colors.textSecondary, style = TrxTypography.subhead)
                if (row.joinedBy.isNotBlank()) {
                    Text(row.joinedBy, color = colors.textTertiary, style = TrxTypography.footnote)
                }
            }
            TrxLargeButton(
                label = UiCopy.remove(),
                kind = TrxButtonKind.Secondary,
                tag = ManageClassTestTags.remove(row.studentId),
                onClick = { onRemove(row.studentId) },
                enabled = true,
                modifier = Modifier.width(REMOVE_WIDTH),
                height = TrxSpacing.touchTarget,
                textStyle = TrxTypography.subheadStrong,
            )
        }
    }
}

private val REMOVE_WIDTH = 104.dp
