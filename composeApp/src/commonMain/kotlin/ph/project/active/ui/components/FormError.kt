package ph.project.active.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

/** Danger row with a "!" mark. Only the message is spoken. */
@Composable
fun FormError(message: String, tag: String, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    Row(
        modifier
            .fillMaxWidth()
            .background(colors.dangerBg, RoundedCornerShape(TrxSpacing.radiusMd))
            .padding(horizontal = TrxSpacing.s16, vertical = TrxSpacing.s12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(TrxSpacing.s20)
                .background(colors.dangerFg, CircleShape)
                .clearAndSetSemantics {},
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "!", color = colors.textInverse, style = TrxTypography.label)
        }
        Text(
            text = message,
            color = colors.textPrimary,
            style = TrxTypography.subhead,
            modifier = Modifier
                .padding(start = TrxSpacing.s8)
                .testTag(tag),
        )
    }
}
