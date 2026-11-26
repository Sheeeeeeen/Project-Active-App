package ph.project.active.ui.platform

import androidx.compose.runtime.Composable

/** One short vibration after a scan that saved attendance. No sound. */
interface ScanHaptics {
    fun success()
}

@Composable
expect fun rememberScanHaptics(): ScanHaptics
