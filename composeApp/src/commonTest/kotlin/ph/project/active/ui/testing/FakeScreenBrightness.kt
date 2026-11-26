package ph.project.active.ui.testing

import ph.project.active.ui.platform.ScreenBrightness

/** Records what the Daily QR tab asked of the screen (flow 5 decision 5, F5-AC5). */
class FakeScreenBrightness : ScreenBrightness {
    /** Every call in order: "raise" or "restore". */
    val calls = mutableListOf<String>()

    /** True while brightness is at full and the screen is kept awake. */
    val isRaised: Boolean get() = calls.lastOrNull() == "raise"

    override fun raise() {
        calls += "raise"
    }

    override fun restore() {
        calls += "restore"
    }
}
