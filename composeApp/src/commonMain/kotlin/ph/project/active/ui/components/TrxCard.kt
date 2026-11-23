package ph.project.active.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing

@Composable
fun TrxCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = TrxSpacing.s16,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalTrxColors.current
    val shape = RoundedCornerShape(TrxSpacing.radiusLg)
    Column(
        modifier
            .fillMaxWidth()
            .background(colors.surface, shape)
            .border(1.dp, colors.borderDefault, shape)
            .padding(contentPadding),
        content = content,
    )
}
