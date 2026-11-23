package ph.project.active.ui.manageclass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.manageclass.ManageClassUiState
import ph.project.active.presentation.manageclass.RequestRow
import ph.project.active.ui.components.EmptyState
import ph.project.active.ui.components.TrxButtonKind
import ph.project.active.ui.components.TrxCard
import ph.project.active.ui.components.TrxLargeButton
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.inbox

@Composable
internal fun RequestsTab(state: ManageClassUiState, onApprove: (String) -> Unit, onDecline: (String) -> Unit) {
    if (state.requestsEmpty) {
        EmptyState(
            icon = Res.drawable.inbox,
            title = UiCopy.requestsEmptyTitle(),
            body = UiCopy.requestsEmptyBody(),
            tag = ManageClassTestTags.requestsEmpty,
        )
        return
    }
    ManageSectionHeader(UiCopy.waitingForYou(), UiCopy.requestCount(state.requests.size))
    state.requests.forEach { row -> RequestCard(row, onApprove, onDecline) }
    ManageFootnote(UiCopy.requestsFootnote())
}

@Composable
private fun RequestCard(row: RequestRow, onApprove: (String) -> Unit, onDecline: (String) -> Unit) {
    val colors = LocalTrxColors.current
    TrxCard(Modifier.padding(top = TrxSpacing.s12)) {
        Text(row.name, color = colors.textPrimary, style = TrxTypography.headline)
        Text(row.employeeId, color = colors.textSecondary, style = TrxTypography.subhead)
        Row(Modifier.padding(top = TrxSpacing.s12), horizontalArrangement = Arrangement.spacedBy(TrxSpacing.s8)) {
            TrxLargeButton(
                label = UiCopy.decline(),
                kind = TrxButtonKind.Secondary,
                tag = ManageClassTestTags.decline(row.studentId),
                onClick = { onDecline(row.studentId) },
                enabled = true,
                modifier = Modifier.weight(1f),
                height = TrxSpacing.touchTarget,
                textStyle = TrxTypography.subheadStrong,
            )
            TrxLargeButton(
                label = UiCopy.approve(),
                kind = TrxButtonKind.Primary,
                tag = ManageClassTestTags.approve(row.studentId),
                onClick = { onApprove(row.studentId) },
                enabled = true,
                modifier = Modifier.weight(1f),
                height = TrxSpacing.touchTarget,
                textStyle = TrxTypography.subheadStrong,
            )
        }
    }
}
