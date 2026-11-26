package ph.project.active.ui.testing

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import ph.project.active.ui.dailyqr.QrMatrix

/** Pixels per module when a [QrMatrix] is drawn for the reader. */
private const val SCALE = 4

/**
 * Reads the text a [QrMatrix] holds with zxing (test-only, Planner ruling 2). Dark modules are drawn
 * black and light modules white, so this checks the text the code holds, not a picture of it.
 */
fun decodeQr(matrix: QrMatrix): String {
    val side = matrix.size * SCALE
    val pixels = IntArray(side * side) { index ->
        val x = (index % side) / SCALE
        val y = (index / side) / SCALE
        if (matrix.isDark(x, y)) BLACK else WHITE
    }
    val bitmap = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(side, side, pixels)))
    return QRCodeReader().decode(bitmap, mapOf(DecodeHintType.PURE_BARCODE to true)).text
}

private const val BLACK = 0xFF000000.toInt()
private const val WHITE = 0xFFFFFFFF.toInt()
