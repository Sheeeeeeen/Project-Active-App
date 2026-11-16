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
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ph.project.active.presentation.classdetail.RequestingClassHeader
import ph.project.active.presentation.classdetail.scheduleLine
import ph.project.active.presentation.common.UiCopy
import ph.project.active.ui.registration.RegistrationTestTags
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.dumbbell

@Composable
fun RequestingClassCard(header: RequestingClassHeader, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    val subtitle = header.scheduleLine()
    val kicker = UiCopy.requestingToJoinKicker()
    Row(
        modifier
            .fillMaxWidth()
            .testTag(RegistrationTestTags.requestingCard)
            .semantics(mergeDescendants = true) {
                this[SemanticsProperties.Text] = listOf(
                    AnnotatedString(kicker),
                    AnnotatedString(header.name),
                    AnnotatedString(subtitle),
                )
            }
            .background(colors.accentSubtle, RoundedCornerShape(TrxSpacing.radiusMd))
            .padding(TrxSpacing.s16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.dumbbell),
            contentDescription = null,
            tint = colors.accentText,
            modifier = Modifier
                .padding(end = TrxSpacing.s12)
                .size(20.dp),
        )
        Column(Modifier.clearAndSetSemantics {}) {
            Text(text = kicker, color = colors.accentText, style = TrxTypography.overline)
            Text(
                text = header.name,
                color = colors.textPrimary,
                style = TrxTypography.headline,
                modifier = Modifier.padding(top = TrxSpacing.s4),
            )
            Text(
                text = subtitle,
                color = colors.textSecondary,
                style = TrxTypography.footnote,
                modifier = Modifier.padding(top = TrxSpacing.s4),
            )
        }
    }
}
