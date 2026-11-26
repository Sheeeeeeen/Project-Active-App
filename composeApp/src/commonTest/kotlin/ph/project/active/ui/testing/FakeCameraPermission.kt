package ph.project.active.ui.testing

import ph.project.active.ui.platform.CameraAccess
import ph.project.active.ui.platform.CameraPermission

/**
 * Flow 7 spec section 8: the camera permission stand-in. [access] is what `current()` returns; a
 * `request()` answers with [nextAnswer] (and keeps it as the new [access]).
 */
class FakeCameraPermission(initial: CameraAccess) : CameraPermission {
    var access: CameraAccess = initial
    var nextAnswer: CameraAccess = CameraAccess.GRANTED
    var requestCount = 0
        private set
    var openSettingsCount = 0
        private set

    override fun current(): CameraAccess = access

    override fun request(onResult: (CameraAccess) -> Unit) {
        requestCount += 1
        access = nextAnswer
        onResult(nextAnswer)
    }

    override fun openSettings() {
        openSettingsCount += 1
    }
}
