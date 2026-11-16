package ph.project.active.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

@Composable
fun StatusChip(
    label: String,
    icon: DrawableResource,
    background: Color,
    content: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .semantics { text = AnnotatedString(label) }
            .background(background, CircleShape)
            .padding(horizontal = TrxSpacing.s12, vertical = TrxSpacing.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = content,
            modifier = Modifier
                .padding(end = TrxSpacing.s4)
                .size(TrxSpacing.iconSmall),
        )
        Text(
            text = label,
            color = content,
            style = TrxTypography.label,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}
