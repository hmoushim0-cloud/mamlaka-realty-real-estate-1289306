package sa.factory.mamlakarealtyreale.model

import java.io.Serializable

data class MamlakaRealtyItem(
    val id: String,
    val title: String,
    val description: String,
    val priceSar: Double,
    val badge: String = "معتمد",
    val category: String = "عام",
    val status: String = "AVAILABLE"
) : Serializable

data class MamlakaRealtyOrder(
    val orderId: String,
    val items: List<MamlakaRealtyItem>,
    val subtotalSar: Double,
    val vatSar: Double,
    val totalSar: Double,
    val paymentMethod: String = "Mada Contactless EMV",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "CONFIRMED"
) : Serializable

data class MamlakaRealtyTelemetry(
    val peripheralName: String,
    val status: String,
    val liveReading: String,
    val batteryPct: Int = 98,
    val signalDbm: Int = -58,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

enum class RoomSpaceType {
    MAJLIS_RECEPTION,
    MASTER_SUITE,
    LIVING_SALON,
    DINING_HALL,
    BALCONY_TERRACE,
    CORRIDOR
}

data class LidarPoint2D(
    val xMeters: Double,
    val yMeters: Double,
    val distanceMm: Int,
    val angleDeg: Double,
    val quality: Int
) : Serializable

data class LidarScanPacket(
    val startAngleDeg: Double,
    val endAngleDeg: Double,
    val points: List<LidarPoint2D>,
    val validPointCount: Int,
    val isValid: Boolean,
    val rawFrameHex: String
) : Serializable

data class FloorplanMetrics(
    val roomType: RoomSpaceType,
    val areaSquareMeters: Double,
    val perimeterMeters: Double,
    val vertexCount: Int,
    val boundingBoxWidthM: Double,
    val boundingBoxLengthM: Double,
    val confidenceScore: Double,
    val timestampEpochMs: Long = System.currentTimeMillis()
) : Serializable
