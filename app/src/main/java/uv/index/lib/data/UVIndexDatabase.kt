@file:Suppress("unused")

package uv.index.lib.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UVIndexData::class,
        UVMetaData::class
    ],
    version = 3,
    exportSchema = false
)
abstract class UVIndexDatabase : RoomDatabase() {
    abstract fun getUVIndexDao(): UVIndexDao
    abstract fun getUVMetaDao(): UVMetaDao
}