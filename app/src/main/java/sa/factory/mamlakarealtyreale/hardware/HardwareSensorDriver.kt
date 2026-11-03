package sa.factory.mamlakarealtyreale.hardware

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import sa.factory.mamlakarealtyreale.spatial.SpatialFloorplanEngine
import java.util.Locale

data class HardwareTelemetryFrame(
    val peripheralName: String,
    val connectionState: String,
    val livePayload: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HardwareSensorDriver {
    private val peripheral = "360 LiDAR Spatial Scanner + Matterport Pro3"

    // Generates simulated 360-degree LiDAR sweep binary frames
    // Sweep represents a Saudi luxury reception Majlis (8.5m x 6.2m = ~52.7 m^2)
    private fun createLidarSweepFrame(majlisWidthMm: Int, majlisLengthMm: Int): ByteArray {
        val sampleCount = 36 // 10-degree increments
        val dataLen = 9 + (sampleCount * 3)
        val frame = ByteArray(dataLen + 2)

        frame[0] = LidarSpatialScannerParser.SYNC_BYTE_1
        frame[1] = LidarSpatialScannerParser.SYNC_BYTE_2
        frame[2] = ((dataLen ushr 8) and 0xFF).toByte()
        frame[3] = (dataLen and 0xFF).toByte()
        frame[4] = 0x00.toByte(); frame[5] = 0x00.toByte() // Start angle: 0.00 deg
        frame[6] = 0x8C.toByte(); frame[7] = 0xA0.toByte() // End angle: 360.00 deg (36000 -> 0x8CA0)
        frame[8] = sampleCount.toByte()

        var offset = 9
        for (i in 0 until sampleCount) {
            val angleDeg = i * 10.0
            val rad = Math.toRadians(angleDeg)
            // Raycast distance to rectangle centered at origin
            val halfW = majlisWidthMm.toDouble() / 2.0
            val halfL = majlisLengthMm.toDouble() / 2.0
            val dist = (halfW / Math.max(0.2, Math.abs(Math.cos(rad)))).coerceAtMost(halfL / Math.max(0.2, Math.abs(Math.sin(rad)))).toInt()

            frame[offset] = ((dist ushr 8) and 0xFF).toByte()
            frame[offset + 1] = (dist and 0xFF).toByte()
            frame[offset + 2] = 200.toByte() // High signal quality
            offset += 3
        }

        val crc = LidarSpatialScannerParser.calculateChecksum(frame, dataLen)
        frame[dataLen] = ((crc ushr 8) and 0xFF).toByte()
        frame[dataLen + 1] = (crc and 0xFF).toByte()

        return frame
    }

    fun observePeripheralStream(): Flow<HardwareTelemetryFrame> = flow {
        emit(HardwareTelemetryFrame(peripheral, "CONNECTING", "LiDAR USB/Ethernet Serial Handshake: Matterport Pro3 initializing..."))
        delay(300)
        emit(HardwareTelemetryFrame(peripheral, "ONLINE", "360 LiDAR Spatial Scanner: ONLINE | RPM=600 | 10Hz Sweep Active"))

        val sweepFrames = listOf(
            createLidarSweepFrame(8500, 6200), // Majlis Reception (~52.7 m^2)
            createLidarSweepFrame(6000, 5000), // Master Suite (~30.0 m^2)
            createLidarSweepFrame(4800, 4200), // Living Salon (~20.1 m^2)
            createLidarSweepFrame(6500, 1500)  // Corridor (~9.7 m^2, high aspect ratio)
        )

        var cycleIndex = 0
        while (true) {
            delay(1500)
            val rawBytes = sweepFrames[cycleIndex % sweepFrames.size]
            val scanPacket = LidarSpatialScannerParser.decodeFrame(rawBytes)
            val metrics = SpatialFloorplanEngine.vectorizeFloorplan(scanPacket.points)

            val payload = String.format(
                Locale.US,
                "LiDAR: %s | Area: %.1f m2 | Perim: %.1fm | Dims: %.1fm x %.1fm | Points: %d | Conf: %.2f",
                metrics.roomType.name,
                metrics.areaSquareMeters,
                metrics.perimeterMeters,
                metrics.boundingBoxWidthM,
                metrics.boundingBoxLengthM,
                scanPacket.validPointCount,
                metrics.confidenceScore
            )

            emit(HardwareTelemetryFrame(peripheral, "ACTIVE_STREAMING", payload))
            cycleIndex++
        }
    }
}
