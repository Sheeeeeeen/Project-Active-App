package ph.project.active.ui.dailyqr

import kotlin.math.abs

/** The module grid of one QR symbol while [QrEncoder] fills it (level M). */
internal class QrSymbol(private val version: Int) {
    private val size = version * 4 + 17
    private val modules = Array(size) { BooleanArray(size) }
    private val isFunction = Array(size) { BooleanArray(size) }

    init {
        drawFunctionPatterns()
    }

    fun drawCodewords(codewords: IntArray) {
        var i = 0
        var right = size - 1
        while (right >= 1) {
            if (right == 6) right = 5
            for (vertical in 0 until size) {
                for (j in 0..1) {
                    val x = right - j
                    val upward = (right + 1) and 2 == 0
                    val y = if (upward) size - 1 - vertical else vertical
                    if (!isFunction[y][x] && i < codewords.size * 8) {
                        modules[y][x] = (codewords[i ushr 3] ushr (7 - (i and 7))) and 1 == 1
                        i++
                    }
                }
            }
            right -= 2
        }
    }

    fun applyBestMask() {
        var best = 0
        var bestPenalty = Int.MAX_VALUE
        for (mask in 0..7) {
            applyMask(mask)
            drawFormatBits(mask)
            val penalty = penalty()
            if (penalty < bestPenalty) {
                best = mask
                bestPenalty = penalty
            }
            applyMask(mask)
        }
        applyMask(best)
        drawFormatBits(best)
    }

    fun toMatrix(quietZone: Int): QrMatrix {
        val side = size + quietZone * 2
        val dark = BooleanArray(side * side) { index ->
            val x = index % side - quietZone
            val y = index / side - quietZone
            x in 0 until size && y in 0 until size && modules[y][x]
        }
        return QrMatrix(side, dark)
    }

    private fun drawFunctionPatterns() {
        for (i in 0 until size) {
            setFunction(6, i, i % 2 == 0)
            setFunction(i, 6, i % 2 == 0)
        }
        drawFinder(3, 3)
        drawFinder(size - 4, 3)
        drawFinder(3, size - 4)
        val positions = alignmentPositions()
        val last = positions.size - 1
        for (i in positions.indices) {
            for (j in positions.indices) {
                val nearFinder = (i == 0 && j == 0) || (i == 0 && j == last) || (i == last && j == 0)
                if (!nearFinder) drawAlignment(positions[i], positions[j])
            }
        }
        drawFormatBits(0)
        drawVersion()
    }

    private fun drawFinder(x: Int, y: Int) {
        for (dy in -4..4) {
            for (dx in -4..4) {
                val distance = maxOf(abs(dx), abs(dy))
                val xx = x + dx
                val yy = y + dy
                if (xx in 0 until size && yy in 0 until size) setFunction(xx, yy, distance != 2 && distance != 4)
            }
        }
    }

    private fun drawAlignment(x: Int, y: Int) {
        for (dy in -2..2) {
            for (dx in -2..2) setFunction(x + dx, y + dy, maxOf(abs(dx), abs(dy)) != 1)
        }
    }

    private fun alignmentPositions(): IntArray {
        if (version == 1) return IntArray(0)
        val count = version / 7 + 2
        val step = (version * 8 + count * 3 + 5) / (count * 4 - 4) * 2
        val result = IntArray(count)
        result[0] = 6
        var position = size - 7
        for (i in count - 1 downTo 1) {
            result[i] = position
            position -= step
        }
        return result
    }

    private fun drawFormatBits(mask: Int) {
        val data = LEVEL_M_FORMAT_BITS shl 3 or mask
        var remainder = data
        repeat(10) { remainder = (remainder shl 1) xor ((remainder ushr 9) * 0x537) }
        val bits = (data shl 10 or remainder) xor 0x5412
        fun bit(i: Int) = (bits ushr i) and 1 == 1
        for (i in 0..5) setFunction(8, i, bit(i))
        setFunction(8, 7, bit(6))
        setFunction(8, 8, bit(7))
        setFunction(7, 8, bit(8))
        for (i in 9 until 15) setFunction(14 - i, 8, bit(i))
        for (i in 0 until 8) setFunction(size - 1 - i, 8, bit(i))
        for (i in 8 until 15) setFunction(8, size - 15 + i, bit(i))
        setFunction(8, size - 8, true)
    }

    private fun drawVersion() {
        if (version < 7) return
        var remainder = version
        repeat(12) { remainder = (remainder shl 1) xor ((remainder ushr 11) * 0x1F25) }
        val bits = version shl 12 or remainder
        for (i in 0 until 18) {
            val bit = (bits ushr i) and 1 == 1
            val a = size - 11 + i % 3
            val b = i / 3
            setFunction(a, b, bit)
            setFunction(b, a, bit)
        }
    }

    private fun applyMask(mask: Int) {
        for (y in 0 until size) {
            for (x in 0 until size) {
                val invert = when (mask) {
                    0 -> (x + y) % 2 == 0
                    1 -> y % 2 == 0
                    2 -> x % 3 == 0
                    3 -> (x + y) % 3 == 0
                    4 -> (x / 3 + y / 2) % 2 == 0
                    5 -> x * y % 2 + x * y % 3 == 0
                    6 -> (x * y % 2 + x * y % 3) % 2 == 0
                    else -> ((x + y) % 2 + x * y % 3) % 2 == 0
                }
                if (invert && !isFunction[y][x]) modules[y][x] = !modules[y][x]
            }
        }
    }

    private fun penalty(): Int {
        var result = 0
        for (line in 0 until size) {
            result += linePenalty { modules[line][it] }
            result += linePenalty { modules[it][line] }
        }
        for (y in 0 until size - 1) {
            for (x in 0 until size - 1) {
                val color = modules[y][x]
                if (color == modules[y][x + 1] && color == modules[y + 1][x] && color == modules[y + 1][x + 1]) result += 3
            }
        }
        val dark = modules.sumOf { row -> row.count { it } }
        val total = size * size
        result += ((abs(dark * 20 - total * 10) + total - 1) / total - 1) * 10
        return result
    }

    // Runs of five or more (rule 1) and finder-like 1:1:3:1:1 patterns with four light modules on one side (rule 3).
    private fun linePenalty(at: (Int) -> Boolean): Int {
        var result = 0
        var runLength = 0
        var runColor = false
        for (i in 0 until size) {
            if (i > 0 && at(i) == runColor) {
                runLength++
            } else {
                if (runLength >= 5) result += runLength - 2
                runColor = at(i)
                runLength = 1
            }
        }
        if (runLength >= 5) result += runLength - 2
        fun darkAt(i: Int) = i in 0 until size && at(i)
        for (start in -4 until size) {
            if (FINDER_LIKE.indices.all { darkAt(start + it) == FINDER_LIKE[it] }) result += 40
            if (FINDER_LIKE.indices.all { darkAt(start + it) == FINDER_LIKE[FINDER_LIKE.size - 1 - it] }) result += 40
        }
        return result
    }

    private fun setFunction(x: Int, y: Int, dark: Boolean) {
        modules[y][x] = dark
        isFunction[y][x] = true
    }
}

private const val LEVEL_M_FORMAT_BITS = 0
private val FINDER_LIKE = booleanArrayOf(true, false, true, true, true, false, true, false, false, false, false)
