package ph.project.active.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.info

/** Tinted box with an icon, a bold title and a body line. Not tappable or dismissible. */
@Composable
fun InfoCallout(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    tag: String? = null,
    icon: DrawableResource = Res.drawable.info,
    style: InfoCalloutStyle = InfoCalloutStyle.Info,
) {
    val colors = LocalTrxColors.current
    val background = when (style) {
        InfoCalloutStyle.Info -> colors.infoBg
        InfoCalloutStyle.Neutral -> colors.surfaceAlt
        InfoCalloutStyle.Accent -> colors.accentSubtle
        InfoCalloutStyle.Warning -> colors.warningBg
    }
    val iconTint = when (style) {
        InfoCalloutStyle.Info -> colors.infoFg
        InfoCalloutStyle.Neutral -> colors.textSecondary
        InfoCalloutStyle.Accent -> colors.accentText
        InfoCalloutStyle.Warning -> colors.warningFg
    }
    Row(
        modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(TrxSpacing.radiusMd))
            .then(if (tag != null) Modifier.testTag(tag) else Modifier)
            .padding(TrxSpacing.s16),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .padding(end = TrxSpacing.s12)
                .size(TrxSpacing.s20),
        )
        Column {
            Text(title, color = colors.textPrimary, style = TrxTypography.subheadStrong)
            Text(
                text = body,
                color = colors.textSecondary,
                style = TrxTypography.subhead,
                modifier = Modifier.padding(top = TrxSpacing.s4),
            )
        }
    }
}
