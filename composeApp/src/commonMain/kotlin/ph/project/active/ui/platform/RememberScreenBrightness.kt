package ph.project.active.ui.platform

import androidx.compose.runtime.Composable

/** This platform's [ScreenBrightness] for the current window. */
@Composable
expect fun rememberScreenBrightness(): ScreenBrightness
