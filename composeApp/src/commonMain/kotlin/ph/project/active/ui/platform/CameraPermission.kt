package ph.project.active.ui.platform

import androidx.compose.runtime.Composable

/** Asks for the camera and opens this app's Settings page. */
interface CameraPermission {
    fun current(): CameraAccess
    fun request(onResult: (CameraAccess) -> Unit)
    fun openSettings()
}

@Composable
expect fun rememberCameraPermission(): CameraPermission
