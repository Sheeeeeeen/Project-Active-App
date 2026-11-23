package ph.project.active.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

/** A content-width button for a row action. The visual is short, the touch target stays 48dp. */
@Composable
fun TrxCompactButton(
    label: String,
    kind: TrxButtonKind,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val buttonColors = trxButtonColors(kind, enabled)
    val shape = RoundedCornerShape(TrxSpacing.radiusSm)
    Box(
        modifier
            .minimumInteractiveComponentSize()
            .height(TrxSpacing.s40)
            .clip(shape)
            .background(buttonColors.background)
            .border(width = 1.dp, color = buttonColors.border, shape = shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .testTag(tag)
            .semantics(mergeDescendants = true) {
                text = AnnotatedString(label)
                if (!enabled) disabled()
            }
            .padding(horizontal = TrxSpacing.s16),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = buttonColors.content,
            style = TrxTypography.subheadStrong,
            maxLines = 1,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}
