package ph.project.active.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

@Composable
fun OfflineBanner(message: String, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    val inverse = colors.textPrimary
    val onInverse = if (colors == TrxColors.Dark) Color(0xFF0B0B0C) else Color.White
    Text(
        text = message,
        color = onInverse,
        style = TrxTypography.body,
        modifier = modifier
            .fillMaxWidth()
            .background(inverse)
            .padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s12)
            .testTag(ComponentTestTags.offlineBanner),
    )
}
