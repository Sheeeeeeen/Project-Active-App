package ph.project.active.ui.dailyqr

/**
 * A small pure-Kotlin QR encoder: byte mode, error correction level M, the smallest version (1–40)
 * that fits, and the lowest-penalty mask. It follows ISO/IEC 18004 as laid out in Project Nayuki's
 * public-domain reference encoder. The result has a 4-module quiet zone.
 */
object QrEncoder {
    fun encode(text: String): QrMatrix {
        val bytes = text.encodeToByteArray()
        val version = (1..40).firstOrNull { 4 + countBits(it) + bytes.size * 8 <= dataCodewords(it) * 8 }
            ?: throw IllegalArgumentException("text is too long for a QR code: ${bytes.size} bytes")
        val symbol = QrSymbol(version)
        symbol.drawCodewords(withErrorCorrection(version, dataBits(version, bytes)))
        symbol.applyBestMask()
        return symbol.toMatrix(QUIET_ZONE)
    }

    private fun countBits(version: Int): Int = if (version <= 9) 8 else 16

    private fun dataBits(version: Int, bytes: ByteArray): IntArray {
        val bits = ArrayList<Boolean>()
        fun append(value: Int, length: Int) {
            for (i in length - 1 downTo 0) bits += (value ushr i) and 1 == 1
        }
        append(BYTE_MODE, 4)
        append(bytes.size, countBits(version))
        bytes.forEach { append(it.toInt() and 0xFF, 8) }
        val capacity = dataCodewords(version) * 8
        append(0, minOf(4, capacity - bits.size))
        append(0, (8 - bits.size % 8) % 8)
        var pad = 0xEC
        while (bits.size < capacity) {
            append(pad, 8)
            pad = pad xor 0xEC xor 0x11
        }
        return IntArray(bits.size / 8) { index ->
            (0 until 8).fold(0) { acc, bit -> (acc shl 1) or (if (bits[index * 8 + bit]) 1 else 0) }
        }
    }

    private fun withErrorCorrection(version: Int, data: IntArray): IntArray {
        val blockCount = ERROR_CORRECTION_BLOCKS[version]
        val eccLength = ECC_CODEWORDS_PER_BLOCK[version]
        val rawCodewords = rawDataModules(version) / 8
        val shortBlocks = blockCount - rawCodewords % blockCount
        val shortBlockLength = rawCodewords / blockCount
        val divisor = reedSolomonDivisor(eccLength)
        val blocks = ArrayList<IntArray>()
        var offset = 0
        for (i in 0 until blockCount) {
            val dataLength = shortBlockLength - eccLength + if (i < shortBlocks) 0 else 1
            val blockData = data.copyOfRange(offset, offset + dataLength)
            offset += dataLength
            val block = blockData.copyOf(shortBlockLength + 1)
            reedSolomonRemainder(blockData, divisor).copyInto(block, block.size - eccLength)
            blocks += block
        }
        val result = IntArray(rawCodewords)
        var k = 0
        for (i in blocks[0].indices) {
            for (j in blocks.indices) {
                // Short blocks have no codeword at the position where long blocks hold their extra data byte.
                if (i != shortBlockLength - eccLength || j >= shortBlocks) {
                    result[k++] = blocks[j][i]
                }
            }
        }
        return result
    }

    private fun reedSolomonDivisor(degree: Int): IntArray {
        val result = IntArray(degree)
        result[degree - 1] = 1
        var root = 1
        repeat(degree) {
            for (j in 0 until degree) {
                result[j] = gfMultiply(result[j], root)
                if (j + 1 < degree) result[j] = result[j] xor result[j + 1]
            }
            root = gfMultiply(root, 0x02)
        }
        return result
    }

    private fun reedSolomonRemainder(data: IntArray, divisor: IntArray): IntArray {
        val result = IntArray(divisor.size)
        for (value in data) {
            val factor = value xor result[0]
            result.copyInto(result, 0, 1)
            result[result.size - 1] = 0
            for (i in result.indices) result[i] = result[i] xor gfMultiply(divisor[i], factor)
        }
        return result
    }

    private fun gfMultiply(x: Int, y: Int): Int {
        var z = 0
        for (i in 7 downTo 0) {
            z = (z shl 1) xor ((z ushr 7) * 0x11D)
            z = z xor (((y ushr i) and 1) * x)
        }
        return z
    }

    private fun dataCodewords(version: Int): Int =
        rawDataModules(version) / 8 - ECC_CODEWORDS_PER_BLOCK[version] * ERROR_CORRECTION_BLOCKS[version]

    private fun rawDataModules(version: Int): Int {
        var result = (16 * version + 128) * version + 64
        if (version >= 2) {
            val alignmentCount = version / 7 + 2
            result -= (25 * alignmentCount - 10) * alignmentCount - 55
            if (version >= 7) result -= 36
        }
        return result
    }
}

private const val QUIET_ZONE = 4
private const val BYTE_MODE = 0b0100

// Level M, indexed by version (index 0 is unused).
private val ECC_CODEWORDS_PER_BLOCK = intArrayOf(
    -1, 10, 16, 26, 18, 24, 16, 18, 22, 22, 26, 30, 22, 22, 24, 24, 28, 28, 26, 26, 26,
    26, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28,
)
private val ERROR_CORRECTION_BLOCKS = intArrayOf(
    -1, 1, 1, 1, 2, 2, 4, 4, 4, 5, 5, 5, 8, 9, 9, 10, 10, 11, 13, 14, 16,
    17, 17, 18, 20, 21, 23, 25, 26, 28, 29, 31, 33, 35, 37, 38, 40, 43, 45, 47, 49,
)
