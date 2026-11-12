package ph.project.active.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

@Composable
fun OfflineBanner(message: String, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    Row(
        modifier
            .fillMaxWidth()
            .background(colors.surfaceInverse)
            .padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.WifiOff,
            contentDescription = null,
            tint = colors.textInverse,
            modifier = Modifier
                .padding(end = TrxSpacing.s8)
                .size(20.dp),
        )
        Text(
            text = message,
            color = colors.textInverse,
            style = TrxTypography.body,
            modifier = Modifier.testTag(ComponentTestTags.offlineBanner),
        )
    }
}
