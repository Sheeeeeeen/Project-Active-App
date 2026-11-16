package ph.project.active.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

@Composable
fun TrxLargeButton(
    label: String,
    kind: TrxButtonKind,
    tag: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    progressTag: String? = null,
    icon: DrawableResource? = null,
    height: Dp = TrxSpacing.buttonHeight,
    textStyle: TextStyle = TrxTypography.button,
) {
    val colors = LocalTrxColors.current
    val shape = RoundedCornerShape(TrxSpacing.radiusMd)
    val background = when {
        !enabled && kind == TrxButtonKind.Primary -> colors.surfaceAlt
        kind == TrxButtonKind.Primary -> colors.accentDefault
        else -> colors.surface
    }
    val content = when {
        !enabled && kind == TrxButtonKind.Primary -> colors.textTertiary
        kind == TrxButtonKind.Primary -> colors.textOnAccent
        else -> colors.textPrimary
    }
    val borderColor = if (kind == TrxButtonKind.Secondary) colors.borderStrong else Color.Transparent
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(background)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .testTag(tag)
            .semantics(mergeDescendants = true) {
                text = AnnotatedString(label)
                if (!enabled) disabled()
            },
        contentAlignment = Alignment.Center,
    ) {
        if (loading && progressTag != null) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(18.dp)
                    .clearAndSetSemantics { testTag = progressTag },
                color = content,
                strokeWidth = 2.dp,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null && !loading) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier
                        .padding(end = TrxSpacing.s8)
                        .size(20.dp),
                )
            }
            Text(
                text = label,
                color = content,
                style = textStyle,
                modifier = Modifier.clearAndSetSemantics {},
            )
        }
    }
}
