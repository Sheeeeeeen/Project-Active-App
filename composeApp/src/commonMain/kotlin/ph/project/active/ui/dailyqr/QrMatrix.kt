package ph.project.active.ui.dailyqr

/** A square grid of QR modules, quiet zone included. (0, 0) is the top-left module. */
class QrMatrix(val size: Int, private val dark: BooleanArray) {
    init {
        require(dark.size == size * size) { "expected ${size * size} modules, got ${dark.size}" }
    }

    fun isDark(x: Int, y: Int): Boolean = dark[y * size + x]
}
