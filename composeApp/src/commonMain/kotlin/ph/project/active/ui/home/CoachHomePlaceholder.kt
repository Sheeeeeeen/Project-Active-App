package ph.project.active.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

@Composable
fun CoachHomePlaceholder(name: String, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    Text(
        text = name,
        color = colors.textPrimary,
        style = TrxTypography.title,
        modifier = modifier
            .padding(TrxSpacing.screenHorizontal)
            .testTag(HomeTestTags.coachHome),
    )
}
