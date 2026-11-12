package ph.project.active.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

@Composable
fun StudentHomePlaceholder(
    name: String,
    openClassId: String?,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTrxColors.current
    Column(
        modifier
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(TrxSpacing.screenHorizontal),
    ) {
        Text(
            text = name,
            color = colors.textPrimary,
            style = TrxTypography.title,
            modifier = Modifier.testTag(HomeTestTags.studentHome),
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
