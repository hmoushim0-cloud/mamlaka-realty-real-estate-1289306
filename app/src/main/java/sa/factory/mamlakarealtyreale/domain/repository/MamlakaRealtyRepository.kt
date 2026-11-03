package sa.factory.mamlakarealtyreale.domain.repository

import sa.factory.mamlakarealtyreale.model.MamlakaRealtyItem
import sa.factory.mamlakarealtyreale.model.MamlakaRealtyOrder
import sa.factory.mamlakarealtyreale.model.MamlakaRealtyTelemetry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MamlakaRealtyRepository {
    val catalogFlow: Flow<List<MamlakaRealtyItem>>
    val activeCart: StateFlow<List<MamlakaRealtyItem>>
    val telemetryFlow: Flow<MamlakaRealtyTelemetry>
    
    fun addToCart(item: MamlakaRealtyItem)
    fun removeFromCart(item: MamlakaRealtyItem)
    fun clearCart()
    suspend fun checkout(paymentMethod: String): MamlakaRealtyOrder
    suspend fun refreshCatalog()
}
