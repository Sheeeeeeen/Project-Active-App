package ph.project.active.testkit

import kotlin.time.Clock
import kotlin.time.Instant

class FakeClock(var current: Instant) : Clock {
    override fun now(): Instant = current
}
