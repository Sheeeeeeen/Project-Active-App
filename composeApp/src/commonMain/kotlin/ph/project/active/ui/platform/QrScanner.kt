package ph.project.active.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Hands the scanned text back to the screen. Android, iOS and desktop each supply one. */
interface QrScanner {
    val hasTorch: Boolean

    @Composable
    fun Preview(torchOn: Boolean, onText: (String) -> Unit, modifier: Modifier)
}

@Composable
expect fun rememberQrScanner(): QrScanner
