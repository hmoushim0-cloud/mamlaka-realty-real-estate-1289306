package sa.factory.mamlakarealtyreale.data.repository

import sa.factory.mamlakarealtyreale.domain.repository.MamlakaRealtyRepository
import sa.factory.mamlakarealtyreale.hardware.HardwareSensorDriver
import sa.factory.mamlakarealtyreale.model.MamlakaRealtyItem
import sa.factory.mamlakarealtyreale.model.MamlakaRealtyOrder
import sa.factory.mamlakarealtyreale.model.MamlakaRealtyTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MamlakaRealtyRepositoryImpl(
    private val sensorDriver: HardwareSensorDriver
) : MamlakaRealtyRepository {

    private val _catalog = MutableStateFlow<List<MamlakaRealtyItem>>(listOf(
        MamlakaRealtyItem("1", "الباقة التنفيذية المعتمدة", "تكامل مباشر مع 360 LiDAR Spatial Scanner + Matterport Pro3", 180.0, "معتمد", "الخدمات الرئيسية"),
        MamlakaRealtyItem("2", "خدمة المعالجة الفورية", "أداء أصيل بسرعة 60 إطار في الثانية", 95.0, "فوري", "المراقبة الحية"),
        MamlakaRealtyItem("3", "باقة الصيانة والدعم السحابي", "تشفير كامل للبيانات المحلية ومزامنة آمنة", 250.0, "شامل", "التقارير الضريبية")
    ))
    override val catalogFlow: Flow<List<MamlakaRealtyItem>> = _catalog.asStateFlow()

    private val _cart = MutableStateFlow<List<MamlakaRealtyItem>>(emptyList())
    override val activeCart: StateFlow<List<MamlakaRealtyItem>> = _cart.asStateFlow()

    override val telemetryFlow: Flow<MamlakaRealtyTelemetry> = sensorDriver.observePeripheralStream()
        .map { frame ->
            MamlakaRealtyTelemetry(
                peripheralName = frame.peripheralName,
                status = frame.connectionState,
                liveReading = frame.livePayload,
                batteryPct = 96,
                signalDbm = -54
            )
        }

    override fun addToCart(item: MamlakaRealtyItem) {
        val current = _cart.value.toMutableList()
        current.add(item)
        _cart.value = current
    }

    override fun removeFromCart(item: MamlakaRealtyItem) {
        val current = _cart.value.toMutableList()
        current.remove(item)
        _cart.value = current
    }

    override fun clearCart() {
        _cart.value = emptyList()
    }

    override suspend fun checkout(paymentMethod: String): MamlakaRealtyOrder {
        val currentItems = _cart.value.toList()
        val subtotal = currentItems.sumOf { it.priceSar }
        val vat = subtotal * 0.15
        val total = subtotal + vat
        val order = MamlakaRealtyOrder(
            orderId = "ORD-${System.currentTimeMillis() % 100000}",
            items = currentItems,
            subtotalSar = subtotal,
            vatSar = vat,
            totalSar = total,
            paymentMethod = paymentMethod
        )
        clearCart()
        return order
    }

    override suspend fun refreshCatalog() {
        // Offline-first sync logic
    }
}
