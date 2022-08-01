package uv.index.lib.data

import androidx.room.Entity

@Entity(tableName = "uv_metadata", primaryKeys = ["longitude", "latitude"])
data class UVMetaData(
    val longitude: Int,
    val latitude: Int,
    val lastSuccessTime: Long
)