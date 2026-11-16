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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

@Composable
fun StatusCard(
    label: String?,
    icon: DrawableResource,
    background: Color,
    accent: Color,
    title: String?,
    body: String,
    titleTag: String,
    bodyTag: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTrxColors.current
    Column(
        modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(TrxSpacing.radiusLg))
            .padding(TrxSpacing.s16),
    ) {
        if (label != null) {
            Row(Modifier.padding(bottom = TrxSpacing.s8), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier
                        .padding(end = TrxSpacing.s8)
                        .size(20.dp),
                )
                Text(label, color = accent, style = TrxTypography.headline)
            }
        }
        if (title != null) {
            Text(
                text = title,
                color = colors.textPrimary,
                style = TrxTypography.sectionTitle,
                modifier = Modifier
                    .testTag(titleTag)
                    .semantics { heading() },
            )
        }
        Text(
            text = body,
            color = colors.textSecondary,
            style = TrxTypography.body,
            modifier = Modifier
                .padding(top = if (title != null) TrxSpacing.s8 else 0.dp)
                .testTag(bodyTag),
        )
    }
}
