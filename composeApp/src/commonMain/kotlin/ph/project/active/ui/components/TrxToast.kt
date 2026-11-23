package ph.project.active.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.painterResource
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.circle_check

/** Dark confirmation row. The caller removes it after a few seconds. */
@Composable
fun TrxToast(message: String, tag: String, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    Row(
        modifier
            .fillMaxWidth()
            .background(colors.surfaceInverse, RoundedCornerShape(TrxSpacing.radiusMd))
            .padding(TrxSpacing.s16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.circle_check),
            contentDescription = null,
            tint = colors.textInverse,
            modifier = Modifier.size(TrxSpacing.s20),
        )
        Text(
            text = message,
            color = colors.textInverse,
            style = TrxTypography.subheadStrong,
            modifier = Modifier
                .padding(start = TrxSpacing.s12)
                .testTag(tag),
        )
    }
}
