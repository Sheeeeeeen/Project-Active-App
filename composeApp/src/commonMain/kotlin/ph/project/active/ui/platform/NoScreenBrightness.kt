package ph.project.active.ui.platform

/** For hosts with no screen brightness to change, such as desktop. */
object NoScreenBrightness : ScreenBrightness {
    override fun raise() = Unit

    override fun restore() = Unit
}
