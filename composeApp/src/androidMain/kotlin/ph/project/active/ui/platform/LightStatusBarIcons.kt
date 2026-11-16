package ph.project.active.ui.platform

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun LightStatusBarIcons(lightIcons: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, lightIcons) {
        val window = (view.context as? Activity)?.window
        if (window == null) {
            onDispose {}
        } else {
            val controller = WindowCompat.getInsetsController(window, view)
            val previous = controller.isAppearanceLightStatusBars
            controller.isAppearanceLightStatusBars = !lightIcons
            onDispose { controller.isAppearanceLightStatusBars = previous }
        }
    }
}
