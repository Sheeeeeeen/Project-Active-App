package ph.project.active.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ph.project.active.ui.components.Placeholder
import ph.project.active.ui.platform.LightStatusBarIcons
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

@Composable
fun StudentHomePlaceholder(
    name: String,
    openClassId: String?,
    modifier: Modifier = Modifier,
) {
    // Placeholder: the student class list replaces this in flow 3.
    LightStatusBarIcons(lightIcons = false)
    val colors = LocalTrxColors.current
    Placeholder(modifier) {
        Scaffold(
            containerColor = colors.canvas,
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { innerPadding ->
            Column(
                Modifier
                    .padding(innerPadding)
                    .padding(TrxSpacing.screenHorizontal),
            ) {
                Text(
                    text = name,
                    color = colors.textPrimary,
                    style = TrxTypography.title,
                    modifier = Modifier
                        .padding(end = PlaceholderTagClearance)
                        .testTag(HomeTestTags.studentHome),
                )
                if (openClassId != null) {
                    Text(
                        text = openClassId,
                        color = colors.textSecondary,
                        style = TrxTypography.body,
                        modifier = Modifier
                            .padding(top = TrxSpacing.s8)
                            .testTag(HomeTestTags.studentOpenClass),
                    )
                }
            }
        }
    }
}

private val PlaceholderTagClearance = 96.dp
