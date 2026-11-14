package ph.project.active.ui.platform

import kotlin.math.sqrt

private const val GRAVITY = 9.81

class ShakeDetector(
    private val threshold: Double,
    private val window: Long,
    private val minHits: Int,
    private val cooldown: Long,
) {
    private val hits = ArrayDeque<Long>()
    private var lastShakeAt: Long? = null

    fun onSample(x: Double, y: Double, z: Double, timestamp: Long): Boolean {
        val magnitude = sqrt(x * x + y * y + z * z)
        if (magnitude - GRAVITY > threshold) {
            hits.addLast(timestamp)
        }
        while (hits.isNotEmpty() && timestamp - hits.first() > window) {
            hits.removeFirst()
        }
        val coolingDown = lastShakeAt?.let { timestamp - it < cooldown } == true
        if (coolingDown || hits.size < minHits) return false
        lastShakeAt = timestamp
        hits.clear()
        return true
    }
}
