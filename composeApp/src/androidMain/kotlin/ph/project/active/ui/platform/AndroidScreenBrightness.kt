package ph.project.active.ui.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/** Sets the activity window's brightness override to full and FLAG_KEEP_SCREEN_ON, then undoes both. */
class AndroidScreenBrightness(private val window: Window) : ScreenBrightness {
    private var previous: Float? = null

    override fun raise() {
        if (previous == null) previous = window.attributes.screenBrightness
        window.attributes = window.attributes.apply {
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun restore() {
        val before = previous ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        previous = null
        window.attributes = window.attributes.apply { screenBrightness = before }
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

@Composable
actual fun rememberScreenBrightness(): ScreenBrightness {
    val view = LocalView.current
    return remember(view) {
        view.context.findActivity()?.window?.let { AndroidScreenBrightness(it) } ?: NoScreenBrightness
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
