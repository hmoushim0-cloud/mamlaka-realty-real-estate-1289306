package sa.factory.mamlakarealtyreale

import sa.factory.mamlakarealtyreale.hardware.LidarSpatialScannerParser
import sa.factory.mamlakarealtyreale.model.LidarPoint2D
import sa.factory.mamlakarealtyreale.model.RoomSpaceType
import sa.factory.mamlakarealtyreale.spatial.SpatialFloorplanEngine
import org.junit.Assert.*
import org.junit.Test

/**
 * Feature Test Suite for Mamlaka Realty 360 LiDAR Spatial Scanner & Floorplan Engine.
 * Zero-Emoji Invariant strictly maintained.
 */
class MamlakaRealtyFeatureTest {

    @Test
    fun testLidarBinaryPacketDecodingAndCoordinates() {
        // Construct frame with 4 cardinal points (0, 90, 180, 270 deg)
        // Sample count: 4. Data length: 9 + 4*3 = 21 bytes. Total frame: 23 bytes.
        val dataLen = 21
        val frame = ByteArray(dataLen + 2)
        frame[0] = LidarSpatialScannerParser.SYNC_BYTE_1
        frame[1] = LidarSpatialScannerParser.SYNC_BYTE_2
        frame[2] = ((dataLen ushr 8) and 0xFF).toByte()
        frame[3] = (dataLen and 0xFF).toByte()
        frame[4] = 0x00.toByte(); frame[5] = 0x00.toByte() // Start: 0.00 deg
        frame[6] = 0x69.toByte(); frame[7] = 0x78.toByte() // End: 270.00 deg (27000 -> 0x6978)
        frame[8] = 0x04.toByte() // 4 samples

        // Pt 0: Angle 0 deg, Dist 5000 mm (5.0m), Quality 100
        frame[9] = 0x13.toByte(); frame[10] = 0x88.toByte(); frame[11] = 100.toByte()
        // Pt 1: Angle 90 deg, Dist 4000 mm (4.0m), Quality 100
        frame[12] = 0x0F.toByte(); frame[13] = 0xA0.toByte(); frame[14] = 100.toByte()
        // Pt 2: Angle 180 deg, Dist 5000 mm (5.0m), Quality 100
        frame[15] = 0x13.toByte(); frame[16] = 0x88.toByte(); frame[17] = 100.toByte()
        // Pt 3: Angle 270 deg, Dist 4000 mm (4.0m), Quality 100
        frame[18] = 0x0F.toByte(); frame[19] = 0xA0.toByte(); frame[20] = 100.toByte()

        val crc = LidarSpatialScannerParser.calculateChecksum(frame, dataLen)
        frame[dataLen] = ((crc ushr 8) and 0xFF).toByte()
        frame[dataLen + 1] = (crc and 0xFF).toByte()

        val packet = LidarSpatialScannerParser.decodeFrame(frame)

        assertTrue("LiDAR frame must be valid", packet.isValid)
        assertEquals("Must decode 4 valid points", 4, packet.validPointCount)

        // Point 0 (0 deg): x = 5.0m, y = 0.0m
        assertEquals(5.0, packet.points[0].xMeters, 0.05)
        assertEquals(0.0, packet.points[0].yMeters, 0.05)

        // Point 1 (90 deg): x = 0.0m, y = 4.0m
        assertEquals(0.0, packet.points[1].xMeters, 0.05)
        assertEquals(4.0, packet.points[1].yMeters, 0.05)

        // Point 2 (180 deg): x = -5.0m, y = 0.0m
        assertEquals(-5.0, packet.points[2].xMeters, 0.05)
        assertEquals(0.0, packet.points[2].yMeters, 0.05)

        // Point 3 (270 deg): x = 0.0m, y = -4.0m
        assertEquals(0.0, packet.points[3].xMeters, 0.05)
        assertEquals(-4.0, packet.points[3].yMeters, 0.05)
    }

    @Test
    fun testLidarNoiseAndNearFieldRejection() {
        val dataLen = 18 // 3 samples
        val frame = ByteArray(dataLen + 2)
        frame[0] = LidarSpatialScannerParser.SYNC_BYTE_1
        frame[1] = LidarSpatialScannerParser.SYNC_BYTE_2
        frame[2] = 0.toByte(); frame[3] = dataLen.toByte()
        frame[4] = 0.toByte(); frame[5] = 0.toByte()
        frame[6] = 0.toByte(); frame[7] = 180.toByte()
        frame[8] = 3.toByte()

        // Sample 1: Low quality (5 < 15) -> Should be rejected
        frame[9] = 0x0F.toByte(); frame[10] = 0xA0.toByte(); frame[11] = 5.toByte()
        // Sample 2: Near blind spot (100 mm < 150 mm) -> Should be rejected
        frame[12] = 0x00.toByte(); frame[13] = 0x64.toByte(); frame[14] = 100.toByte()
        // Sample 3: Legitimate sample (3500 mm, quality 80) -> Valid
        frame[15] = 0x0D.toByte(); frame[16] = 0xAC.toByte(); frame[17] = 80.toByte()

        val crc = LidarSpatialScannerParser.calculateChecksum(frame, dataLen)
        frame[dataLen] = ((crc ushr 8) and 0xFF).toByte()
        frame[dataLen + 1] = (crc and 0xFF).toByte()

        val packet = LidarSpatialScannerParser.decodeFrame(frame)

        assertTrue("Frame must be valid", packet.isValid)
        assertEquals("Only 1 legitimate sample should pass filters", 1, packet.validPointCount)
        assertEquals(3500, packet.points[0].distanceMm)
    }

    @Test
    fun testShoelacePolygonAreaAndPerimeterCalculation() {
        // Rectangle 10m x 6m -> Area = 60 m^2, Perimeter = 32m
        val rectVertices = listOf(
            Pair(0.0, 0.0),
            Pair(10.0, 0.0),
            Pair(10.0, 6.0),
            Pair(0.0, 6.0)
        )
        val rectArea = SpatialFloorplanEngine.calculatePolygonArea(rectVertices)
        val rectPerimeter = SpatialFloorplanEngine.calculatePerimeter(rectVertices)

        assertEquals("Rectangle area must be exactly 60.0 m2", 60.0, rectArea, 0.001)
        assertEquals("Rectangle perimeter must be 32.0 m", 32.0, rectPerimeter, 0.001)

        // Right triangle (0,0), (6,0), (0,8) -> Area = 24 m^2, Perimeter = 24m
        val triVertices = listOf(
            Pair(0.0, 0.0),
            Pair(6.0, 0.0),
            Pair(0.0, 8.0)
        )
        val triArea = SpatialFloorplanEngine.calculatePolygonArea(triVertices)
        val triPerimeter = SpatialFloorplanEngine.calculatePerimeter(triVertices)

        assertEquals("Triangle area must be 24.0 m2", 24.0, triArea, 0.001)
        assertEquals("Triangle perimeter must be 24.0 m", 24.0, triPerimeter, 0.001)
    }

    @Test
    fun testRoomTypeClassificationHeuristic() {
        // Large area -> Majlis Reception
        assertEquals(RoomSpaceType.MAJLIS_RECEPTION, SpatialFloorplanEngine.classifySpaceType(55.0, 8.0, 7.0))

        // Medium large -> Master Suite
        assertEquals(RoomSpaceType.MASTER_SUITE, SpatialFloorplanEngine.classifySpaceType(32.0, 6.0, 5.3))

        // Medium -> Living Salon
        assertEquals(RoomSpaceType.LIVING_SALON, SpatialFloorplanEngine.classifySpaceType(22.0, 5.0, 4.4))

        // Elongated corridor -> Corridor
        assertEquals(RoomSpaceType.CORRIDOR, SpatialFloorplanEngine.classifySpaceType(9.0, 1.2, 7.5))
    }

    @Test
    fun testFloorplanVectorizationFromPointcloud() {
        // Create 4 points representing room boundaries: width = 8.0m (-4 to 4), length = 6.0m (-3 to 3)
        val points = listOf(
            LidarPoint2D(xMeters = -4.0, yMeters = -3.0, distanceMm = 5000, angleDeg = 216.0, quality = 100),
            LidarPoint2D(xMeters = 4.0, yMeters = -3.0, distanceMm = 5000, angleDeg = 324.0, quality = 100),
            LidarPoint2D(xMeters = 4.0, yMeters = 3.0, distanceMm = 5000, angleDeg = 36.0, quality = 100),
            LidarPoint2D(xMeters = -4.0, yMeters = 3.0, distanceMm = 5000, angleDeg = 144.0, quality = 100)
        )

        val metrics = SpatialFloorplanEngine.vectorizeFloorplan(points)

        assertEquals("Width must be 8.0 m", 8.0, metrics.boundingBoxWidthM, 0.01)
        assertEquals("Length must be 6.0 m", 6.0, metrics.boundingBoxLengthM, 0.01)
        assertEquals("Area must be 48.0 m2", 48.0, metrics.areaSquareMeters, 0.01)
        assertEquals("Perimeter must be 28.0 m", 28.0, metrics.perimeterMeters, 0.01)
        assertEquals("Category must be MAJLIS_RECEPTION", RoomSpaceType.MAJLIS_RECEPTION, metrics.roomType)
    }

    @Test
    fun testCorruptedLidarHeaderRejection() {
        val corruptedBytes = byteArrayOf(0x00, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        val packet = LidarSpatialScannerParser.decodeFrame(corruptedBytes)
        assertFalse("Corrupted sync header must be rejected", packet.isValid)
        assertEquals(0, packet.validPointCount)
    }
}
