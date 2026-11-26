package ph.project.active.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIApplication
import platform.UIKit.UIScreen
import platform.UIKit.UIWindowScene

/**
 * Sets the window scene's screen to full brightness and disables the idle timer, then undoes both.
 * Backgrounding the app stops the lifecycle, so the QR tab calls restore() then too.
 */
class IosScreenBrightness : ScreenBrightness {
    private var previous: Double? = null
    private var savedIdleTimer: Boolean? = null

    override fun raise() {
        val screen = currentScreen()
        if (screen != null) {
            if (previous == null) previous = screen.brightness
            screen.brightness = 1.0
        }
        val application = UIApplication.sharedApplication
        if (savedIdleTimer == null) savedIdleTimer = application.idleTimerDisabled
        application.idleTimerDisabled = true
    }

    override fun restore() {
        val before = previous
        previous = null
        if (before != null) currentScreen()?.brightness = before
        val savedIdleTimer = savedIdleTimer
        this.savedIdleTimer = null
        if (savedIdleTimer != null) UIApplication.sharedApplication.idleTimerDisabled = savedIdleTimer
    }

    private fun currentScreen(): UIScreen? =
        UIApplication.sharedApplication.connectedScenes.firstOrNull { it is UIWindowScene }
            ?.let { (it as UIWindowScene).screen }
}

@Composable
actual fun rememberScreenBrightness(): ScreenBrightness = remember { IosScreenBrightness() }
