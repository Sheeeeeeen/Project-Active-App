package ph.project.active.ui.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun rememberQrScanner(): QrScanner = remember { JvmQrScanner() }

private class JvmQrScanner : QrScanner {
    override val hasTorch: Boolean = false

    @Composable
    override fun Preview(torchOn: Boolean, onText: (String) -> Unit, modifier: Modifier) {
        Box(modifier.background(Color.Black))
    }
}
