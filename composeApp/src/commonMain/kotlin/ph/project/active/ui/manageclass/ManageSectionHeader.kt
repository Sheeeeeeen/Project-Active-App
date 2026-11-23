package ph.project.active.ui.manageclass

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

@Composable
internal fun ManageSectionHeader(title: String, count: String, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    Row(
        modifier
            .fillMaxWidth()
            .padding(top = TrxSpacing.s8, bottom = TrxSpacing.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = colors.textSecondary, style = TrxTypography.overline, modifier = Modifier.weight(1f))
        Text(count, color = colors.textTertiary, style = TrxTypography.footnote)
    }
}

@Composable
internal fun ManageFootnote(text: String) {
    Text(
        text = text,
        color = LocalTrxColors.current.textTertiary,
        style = TrxTypography.footnote,
        modifier = Modifier.padding(top = TrxSpacing.s16),
    )
}
