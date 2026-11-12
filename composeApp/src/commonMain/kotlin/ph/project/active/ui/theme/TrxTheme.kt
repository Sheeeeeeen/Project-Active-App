package ph.project.active.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun TrxTheme(
    dark: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (dark) TrxColors.Dark else TrxColors.Light
    CompositionLocalProvider(LocalTrxColors provides colors) {
        MaterialTheme(content = content)
    }
}
