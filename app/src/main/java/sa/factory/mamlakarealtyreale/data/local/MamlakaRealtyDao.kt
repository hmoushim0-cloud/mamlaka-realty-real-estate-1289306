package sa.factory.mamlakarealtyreale.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MamlakaRealtyDao {
    @Query("SELECT * FROM real_estate_items_cache ORDER BY id ASC")
    fun observeAllItems(): Flow<List<MamlakaRealtyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<MamlakaRealtyEntity>)

    @Query("DELETE FROM real_estate_items_cache")
    suspend fun clearAll()
}
