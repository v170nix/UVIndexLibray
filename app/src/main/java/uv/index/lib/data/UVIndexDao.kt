package uv.index.lib.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface UVIndexDao {
    @Query("SELECT * FROM uv_index WHERE latitude == :latitude AND longitude == :longitude AND time >= :beginTime ORDER BY time LIMIT 24")
    fun getUVIndexByLonLat(
        longitude: Int,
        latitude: Int,
        beginTime: Long
    ): Flow<List<UVIndexData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg data: UVIndexData)

    @Query("DELETE FROM uv_index WHERE time <= :time")
    suspend fun deleteObsoleteData(time: Long = Instant.now().epochSecond - 86_000 * 2)
}