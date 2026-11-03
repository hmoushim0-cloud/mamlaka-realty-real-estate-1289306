package sa.factory.mamlakarealtyreale.domain.usecase

import sa.factory.mamlakarealtyreale.domain.repository.MamlakaRealtyRepository
import sa.factory.mamlakarealtyreale.model.MamlakaRealtyItem
import sa.factory.mamlakarealtyreale.model.MamlakaRealtyOrder
import sa.factory.mamlakarealtyreale.model.MamlakaRealtyTelemetry
import kotlinx.coroutines.flow.Flow

class GetCatalogUseCase(private val repository: MamlakaRealtyRepository) {
    operator fun invoke(): Flow<List<MamlakaRealtyItem>> = repository.catalogFlow
}

class ManageCartUseCase(private val repository: MamlakaRealtyRepository) {
    val cart = repository.activeCart
    fun add(item: MamlakaRealtyItem) = repository.addToCart(item)
    fun remove(item: MamlakaRealtyItem) = repository.removeFromCart(item)
    fun clear() = repository.clearCart()
}

class ProcessCheckoutUseCase(private val repository: MamlakaRealtyRepository) {
    suspend operator fun invoke(paymentMethod: String = "Mada Contactless EMV"): MamlakaRealtyOrder =
        repository.checkout(paymentMethod)
}

class ObserveHardwareTelemetryUseCase(private val repository: MamlakaRealtyRepository) {
    operator fun invoke(): Flow<MamlakaRealtyTelemetry> = repository.telemetryFlow
}
