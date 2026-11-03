package sa.factory.mamlakarealtyreale.data.remote

import sa.factory.mamlakarealtyreale.model.MamlakaRealtyItem
import sa.factory.mamlakarealtyreale.model.MamlakaRealtyOrder

interface MamlakaRealtyApiService {
    suspend fun fetchCatalog(): List<MamlakaRealtyItem>
    suspend fun submitOrder(order: MamlakaRealtyOrder): Boolean
}
