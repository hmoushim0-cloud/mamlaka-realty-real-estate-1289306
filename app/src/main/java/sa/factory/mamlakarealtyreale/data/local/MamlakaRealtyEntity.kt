package sa.factory.mamlakarealtyreale.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import sa.factory.mamlakarealtyreale.model.MamlakaRealtyItem

@Entity(tableName = "real_estate_items_cache")
data class MamlakaRealtyEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val priceSar: Double,
    val badge: String,
    val category: String,
    val status: String
) {
    fun toDomain(): MamlakaRealtyItem = MamlakaRealtyItem(
        id = id,
        title = title,
        description = description,
        priceSar = priceSar,
        badge = badge,
        category = category,
        status = status
    )
}
