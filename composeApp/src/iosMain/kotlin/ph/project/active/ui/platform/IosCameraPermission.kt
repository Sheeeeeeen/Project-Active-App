package ph.project.active.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

@Composable
actual fun rememberCameraPermission(): CameraPermission = remember { IosCameraPermission() }

@OptIn(ExperimentalForeignApi::class)
private class IosCameraPermission : CameraPermission {
    override fun current(): CameraAccess = when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
        AVAuthorizationStatusAuthorized -> CameraAccess.GRANTED
        AVAuthorizationStatusNotDetermined -> CameraAccess.NOT_ASKED
        AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> CameraAccess.DENIED_FOR_GOOD
        else -> CameraAccess.DENIED_FOR_GOOD
    }

    override fun request(onResult: (CameraAccess) -> Unit) {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
            onResult(if (granted) CameraAccess.GRANTED else CameraAccess.DENIED_FOR_GOOD)
        }
    }

    override fun openSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url)
    }
}
