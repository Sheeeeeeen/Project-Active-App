package ph.project.active.ui.platform

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class F1ShakeDetectorTest {
    @Test
    fun F1_AC22_emitsOneShakeForHitsInTheWindowAndNoneAtRestOrDuringCooldown() {
        val detector = ShakeDetector(threshold = 2.0, window = 1_000L, minHits = 3, cooldown = 5_000L)

        assertFalse(detector.onSample(0.0, 0.0, 9.81, 0L))
        assertFalse(detector.onSample(0.0, 0.0, 9.81, 40L))
        assertFalse(detector.onSample(0.05, -0.02, 9.81, 80L))

        assertFalse(detector.onSample(0.0, 0.0, 30.0, 1_000L))
        assertFalse(detector.onSample(0.0, 0.0, 30.0, 1_100L))
        assertTrue(detector.onSample(0.0, 0.0, 30.0, 1_200L))

        assertFalse(detector.onSample(0.0, 0.0, 30.0, 1_300L))
        assertFalse(detector.onSample(0.0, 0.0, 30.0, 1_400L))
        assertFalse(detector.onSample(0.0, 0.0, 30.0, 1_500L))
    }
}
