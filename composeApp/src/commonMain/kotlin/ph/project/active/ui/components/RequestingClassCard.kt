package ph.project.active.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import ph.project.active.presentation.classdetail.RequestingClassHeader
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.common.formatDays
import ph.project.active.presentation.common.formatSlot
import ph.project.active.ui.registration.RegistrationTestTags
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

@Composable
fun RequestingClassCard(header: RequestingClassHeader, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    val days = formatDays(header.slotsByWeekday.keys)
    val slots = header.slotsByWeekday.values
        .flatten()
        .sortedBy { it.start }
        .map { formatSlot(it) }
        .distinct()
        .joinToString(", ")
    val subtitle = UiCopy.requestingClassSubtitle(header.coachName, days, slots)
    val kicker = UiCopy.requestingToJoinKicker()
    Column(
        modifier
            .fillMaxWidth()
            .testTag(RegistrationTestTags.requestingCard)
            .semantics {
                this[SemanticsProperties.Text] = listOf(
                    AnnotatedString(kicker),
                    AnnotatedString(header.name),
                    AnnotatedString(subtitle),
                )
            }
            .background(colors.surfaceAlt, RoundedCornerShape(TrxSpacing.radiusMd))
            .padding(TrxSpacing.s16),
    ) {
        Text(text = kicker, color = colors.textSecondary, style = TrxTypography.overline)
        Text(
            text = header.name,
            color = colors.textPrimary,
            style = TrxTypography.title,
            modifier = Modifier.padding(top = TrxSpacing.s8),
        )
        Text(
            text = subtitle,
            color = colors.textSecondary,
            style = TrxTypography.body,
            modifier = Modifier.padding(top = TrxSpacing.s4),
        )
    }
}
