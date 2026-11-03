package sa.factory.mamlakarealtyreale.hardware

import sa.factory.mamlakarealtyreale.model.LidarPoint2D
import sa.factory.mamlakarealtyreale.model.LidarScanPacket
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-Precision 360 LiDAR Spatial Scanner & Matterport Pro3 Telemetry Parser.
 * Decodes high-density spatial point clouds and converts polar coordinates into Cartesian 2D coordinates.
 * Zero-Emoji Invariant strictly maintained.
 */
object LidarSpatialScannerParser {

    const val SYNC_BYTE_1: Byte = 0xA5.toByte()
    const val SYNC_BYTE_2: Byte = 0x5A.toByte()

    const val MIN_DISTANCE_MM = 150
    const val MAX_DISTANCE_MM = 25000
    const val MIN_SIGNAL_QUALITY = 15

    /**
     * Verifies 16-bit additive checksum of frame payload.
     */
    fun calculateChecksum(bytes: ByteArray, length: Int): Int {
        var sum = 0
        for (i in 0 until length) {
            sum = (sum + (bytes[i].toInt() and 0xFF)) and 0xFFFF
        }
        return sum
    }

    /**
     * Decodes a binary LiDAR packet frame.
     */
    fun decodeFrame(frame: ByteArray): LidarScanPacket {
        val hexPayload = bytesToHex(frame)

        if (frame.size < 11 || frame[0] != SYNC_BYTE_1 || frame[1] != SYNC_BYTE_2) {
            return LidarScanPacket(
                startAngleDeg = 0.0,
                endAngleDeg = 0.0,
                points = emptyList(),
                validPointCount = 0,
                isValid = false,
                rawFrameHex = hexPayload
            )
        }

        val dataLen = frame.size - 2
        val computedCrc = calculateChecksum(frame, dataLen)
        val receivedHigh = frame[dataLen].toInt() and 0xFF
        val receivedLow = frame[dataLen + 1].toInt() and 0xFF
        val receivedCrc = (receivedHigh shl 8) or receivedLow

        if (computedCrc != receivedCrc) {
            return LidarScanPacket(
                startAngleDeg = 0.0,
                endAngleDeg = 0.0,
                points = emptyList(),
                validPointCount = 0,
                isValid = false,
                rawFrameHex = hexPayload
            )
        }

        val rawStartAngle = ((frame[4].toInt() and 0xFF) shl 8) or (frame[5].toInt() and 0xFF)
        val rawEndAngle = ((frame[6].toInt() and 0xFF) shl 8) or (frame[7].toInt() and 0xFF)
        val startAngleDeg = rawStartAngle.toDouble() * 0.01
        val endAngleDeg = rawEndAngle.toDouble() * 0.01
        val sampleCount = frame[8].toInt() and 0xFF

        val pointList = mutableListOf<LidarPoint2D>()
        var offset = 9

        val angleSpan = if (endAngleDeg >= startAngleDeg) {
            endAngleDeg - startAngleDeg
        } else {
            (endAngleDeg + 360.0) - startAngleDeg
        }

        val angleStep = if (sampleCount > 1) angleSpan / (sampleCount - 1) else 0.0

        for (i in 0 until sampleCount) {
            if (offset + 3 > dataLen) break
            val distMm = ((frame[offset].toInt() and 0xFF) shl 8) or (frame[offset + 1].toInt() and 0xFF)
            val quality = frame[offset + 2].toInt() and 0xFF
            offset += 3

            val currentAngleDeg = (startAngleDeg + (i * angleStep)) % 360.0

            if (quality >= MIN_SIGNAL_QUALITY && distMm in MIN_DISTANCE_MM..MAX_DISTANCE_MM) {
                val rad = Math.toRadians(currentAngleDeg)
                val distM = distMm.toDouble() / 1000.0
                val x = distM * cos(rad)
                val y = distM * sin(rad)

                pointList.add(
                    LidarPoint2D(
                        xMeters = x,
                        yMeters = y,
                        distanceMm = distMm,
                        angleDeg = currentAngleDeg,
                        quality = quality
                    )
                )
            }
        }

        return LidarScanPacket(
            startAngleDeg = startAngleDeg,
            endAngleDeg = endAngleDeg,
            points = pointList,
            validPointCount = pointList.size,
            isValid = true,
            rawFrameHex = hexPayload
        )
    }

    /**
     * Decodes a hexadecimal string representation of a LiDAR frame.
     */
    fun decodeHexPayload(hexString: String): LidarScanPacket {
        val clean = hexString.replace(" ", "").trim()
        val len = clean.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            val h = Character.digit(clean[i], 16)
            val l = Character.digit(clean[i + 1], 16)
            data[i / 2] = ((h shl 4) + l).toByte()
            i += 2
        }
        return decodeFrame(data)
    }

    fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format(Locale.US, "%02X", b))
        }
        return sb.toString()
    }
}
