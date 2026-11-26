package ph.project.active.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
actual fun rememberScanHaptics(): ScanHaptics {
    val feedback = LocalHapticFeedback.current
    return remember(feedback) {
        object : ScanHaptics {
            override fun success() {
                feedback.performHapticFeedback(HapticFeedbackType.Confirm)
            }
        }
    }
}
