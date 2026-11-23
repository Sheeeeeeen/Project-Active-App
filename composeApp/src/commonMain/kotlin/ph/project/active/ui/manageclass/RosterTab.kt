package ph.project.active.ui.manageclass

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.manageclass.ManageClassUiState
import ph.project.active.presentation.manageclass.RosterRow
import ph.project.active.ui.components.TrxButtonKind
import ph.project.active.ui.components.TrxCard
import ph.project.active.ui.components.TrxCompactButton
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

@Composable
internal fun RosterTab(state: ManageClassUiState, onRemove: (String) -> Unit) {
    val colors = LocalTrxColors.current
    ManageSectionHeader(UiCopy.onTheRoster(), UiCopy.studentCount(state.roster.size))
    if (state.roster.isNotEmpty()) {
        TrxCard(Modifier.padding(top = TrxSpacing.s8), contentPadding = TrxSpacing.s0) {
            state.roster.forEachIndexed { index, row ->
                if (index > 0) HorizontalDivider(color = colors.borderDefault)
                RosterRowLine(row, onRemove)
            }
        }
    }
    ManageFootnote(UiCopy.rosterFootnote())
}

@Composable
private fun RosterRowLine(row: RosterRow, onRemove: (String) -> Unit) {
    val colors = LocalTrxColors.current
    PersonLine(
        name = row.name,
        modifier = Modifier.padding(horizontal = TrxSpacing.s16, vertical = TrxSpacing.s12),
        trailing = {
            TrxCompactButton(
                label = UiCopy.remove(),
                kind = TrxButtonKind.Danger,
                tag = ManageClassTestTags.remove(row.studentId),
                onClick = { onRemove(row.studentId) },
                modifier = Modifier.padding(start = TrxSpacing.s8),
            )
        },
    ) {
        // Employee ID and join path stay separate text nodes so each reads on its own.
        Row {
            Text(row.employeeId, color = colors.textSecondary, style = TrxTypography.subhead)
            if (row.joinedBy.isNotBlank()) {
                Text(" \u00B7 ", color = colors.textSecondary, style = TrxTypography.subhead)
                Text(row.joinedBy, color = colors.textSecondary, style = TrxTypography.subhead)
            }
        }
    }
}
