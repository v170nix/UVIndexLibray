package uv.index.lib.data

import java.time.ZoneId

data class UVIPlaceData(
    val zone: ZoneId,
    val latitude: Double,
    val longitude: Double
)