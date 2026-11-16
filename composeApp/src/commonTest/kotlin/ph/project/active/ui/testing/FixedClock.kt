package ph.project.active.ui.testing

import kotlin.time.Clock
import kotlin.time.Instant

class FixedClock(var current: Instant) : Clock {
    override fun now(): Instant = current
}
