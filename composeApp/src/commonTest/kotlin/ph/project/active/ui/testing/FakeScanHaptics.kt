package ph.project.active.ui.testing

import ph.project.active.ui.platform.ScanHaptics

/** Flow 7 spec section 8 and decision 8: counts the one short success vibration. */
class FakeScanHaptics : ScanHaptics {
    var successCount = 0
        private set

    override fun success() {
        successCount += 1
    }
}
