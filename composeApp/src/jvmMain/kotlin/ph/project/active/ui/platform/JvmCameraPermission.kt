package ph.project.active.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberCameraPermission(): CameraPermission = remember { JvmCameraPermission() }

private class JvmCameraPermission : CameraPermission {
    override fun current(): CameraAccess = CameraAccess.GRANTED

    override fun request(onResult: (CameraAccess) -> Unit) {
        onResult(CameraAccess.GRANTED)
    }

    override fun openSettings() = Unit
}
