package ph.project.active.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberScanHaptics(): ScanHaptics = remember { JvmScanHaptics() }

private class JvmScanHaptics : ScanHaptics {
    override fun success() = Unit
}
