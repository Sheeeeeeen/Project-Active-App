package ph.project.active.ui.platform

/** Full brightness and keep-awake while a code is on screen (flow 5 decision 5). */
interface ScreenBrightness {
    /** Turns brightness up to full and keeps the screen awake. */
    fun raise()

    /** Puts back the brightness from before [raise] and lets the screen sleep again. */
    fun restore()
}
