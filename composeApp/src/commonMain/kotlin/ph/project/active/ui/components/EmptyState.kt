package ph.project.active.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

/** Centered icon circle, title and body. The tagged node carries both lines as its text. */
@Composable
fun EmptyState(
    icon: DrawableResource,
    title: String,
    body: String,
    tag: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTrxColors.current
    Column(
        modifier
            .fillMaxWidth()
            .padding(top = TrxSpacing.s32)
            .testTag(tag)
            .semantics {
                this[SemanticsProperties.Text] = listOf(AnnotatedString(title), AnnotatedString(body))
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(TrxSpacing.emptyStateIcon)
                .background(colors.surfaceAlt, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(TrxSpacing.s32),
            )
        }
        Text(
            text = title,
            color = colors.textPrimary,
            style = TrxTypography.sectionTitle,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = TrxSpacing.s16)
                .clearAndSetSemantics {},
        )
        Text(
            text = body,
            color = colors.textSecondary,
            style = TrxTypography.subhead,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = TrxSpacing.s8)
                .clearAndSetSemantics {},
        )
    }
}
