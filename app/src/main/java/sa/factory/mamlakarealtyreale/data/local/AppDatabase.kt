package sa.factory.mamlakarealtyreale.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MamlakaRealtyEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun real_estateDao(): MamlakaRealtyDao
}
