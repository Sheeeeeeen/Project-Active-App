package ph.project.active.ui.manageclass

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ph.project.active.ui.components.InitialsAvatar
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

/** Avatar, name and the lines under it, with an optional trailing action. */
@Composable
internal fun PersonLine(
    name: String,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
    lines: @Composable ColumnScope.() -> Unit,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        InitialsAvatar(name)
        Column(
            Modifier
                .weight(1f)
                .padding(start = TrxSpacing.s12),
        ) {
            Text(name, color = LocalTrxColors.current.textPrimary, style = TrxTypography.headline)
            lines()
        }
        trailing()
    }
}
