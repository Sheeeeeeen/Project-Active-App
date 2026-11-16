package ph.project.active.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import ph.project.active.ui.theme.LocalTrxColors

@Composable
fun PlaceholderTag(modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    Text(
        text = "PLACEHOLDER",
        color = colors.accentText,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        maxLines = 1,
        modifier = modifier
            .height(20.dp)
            .border(1.dp, colors.accentDefault, CircleShape)
            .background(colors.accentSubtle, CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("placeholder_tag")
            .semantics { contentDescription = "Placeholder" },
    )
}

@Composable
fun Placeholder(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        content()
        PlaceholderTag(
            Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 8.dp, end = 16.dp)
                .zIndex(1f),
        )
    }
}
