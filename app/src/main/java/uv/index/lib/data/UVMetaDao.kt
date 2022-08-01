package uv.index.lib.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.time.Instant

@Dao
interface UVMetaDao {
    @Query("SELECT * FROM uv_metadata WHERE latitude == :latitude AND longitude == :longitude LIMIT 1")
    suspend fun getUVMetaByLonLat(
        longitude: Int,
        latitude: Int,
    ): UVMetaData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: UVMetaData)

    @Query("DELETE FROM uv_metadata WHERE lastSuccessTime <= :time")
    suspend fun deleteObsoleteData(time: Long = Instant.now().epochSecond - 86_000 * 10)
}