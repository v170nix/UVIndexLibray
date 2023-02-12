@file:Suppress("unused")

package uv.index.lib.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.roundToInt

class UVIndexRepository(
    private val api: UVIndexAPI,
    private val dao: UVIndexDao,
    private val metaDao: UVMetaDao
) {

    sealed class RemoteUpdateState {
        object None : RemoteUpdateState()
        object Loading : RemoteUpdateState()
        data class Success<T>(val values: T) : RemoteUpdateState()
        data class Failure(val throwable: Throwable) : RemoteUpdateState()
    }

    fun getDataAsFlow(
        longitude: Double,
        latitude: Double,
        dateAtStartDay: ZonedDateTime
    ): Flow<List<UVIndexData>> {
        return dao.getUVIndexByLonLat(
            transformLongitudeToDb(longitude),
            transformLatitudeToDb(latitude),
            dateAtStartDay.toEpochSecond()
        ).distinctUntilChanged()
    }

    suspend fun getForecastData(
        longitude: Double,
        latitude: Double,
        forecastDateAtStartDay: ZonedDateTime
    ): List<UVSummaryDayData> {
        return (0L..2L)
            .asFlow()
            .map { forecastDateAtStartDay.plusDays(it) }
            .map {
                dao.getUVIndexByLonLat(
                    transformLongitudeToDb(longitude),
                    transformLatitudeToDb(latitude),
                    it.toEpochSecond()
                ).firstOrNull()
            }
            .filterNotNull()
            .filter { it.isNotEmpty() }
            .map { UVSummaryDayData.createFromDayList(forecastDateAtStartDay.zone, it) }
            .filterNotNull()
            .toList()
    }

    fun getForecastDataAsFlow(
        longitude: Double,
        latitude: Double,
        forecastDateAtStartDay: ZonedDateTime,
        countDays: Int = 3
    ): Flow<List<UVSummaryDayData>> {

        val flows = buildList<Flow<List<UVIndexData>>> {
            (0 until countDays).map {
                dao.getUVIndexByLonLat(
                    transformLongitudeToDb(longitude),
                    transformLatitudeToDb(latitude),
                    forecastDateAtStartDay.plusDays(1 + it.toLong()).toEpochSecond()
                )
            }
        }

        return combine(flows.toList()) { arrays ->
            arrays.mapNotNull {
                UVSummaryDayData.createFromDayList(forecastDateAtStartDay.zone, it)
            }
        }
    }

    fun updateFromRemoteAsFlow(
        longitude: Double,
        latitude: Double,
        dateAtStartDay: ZonedDateTime
    ): Flow<RemoteUpdateState> = flow {
        runCatching {
            emit(RemoteUpdateState.Loading)
            val data = getRemoteData(longitude, latitude, dateAtStartDay)
            dao.insertAll(*data.toTypedArray())
            metaDao.insert(
                UVMetaData(
                    longitude = transformLongitudeToDb(longitude),
                    latitude = transformLatitudeToDb(latitude),
                    lastSuccessTime = getCurrentTime()
                )
            )
            dao.deleteObsoleteData()
            metaDao.deleteObsoleteData()

        }.onSuccess { data ->
            emit(RemoteUpdateState.Success(data))
        }.onFailure {
            if (it is CancellationException) throw it
            emit(RemoteUpdateState.Failure(it))
        }
    }

    suspend fun checkForRemoteUpdateData(
        longitude: Double,
        latitude: Double,
    ): Boolean {
        val meta = metaDao.getUVMetaByLonLat(
            transformLongitudeToDb(longitude),
            transformLatitudeToDb(latitude)
        ) ?: return true

        val utcZone = ZoneId.of("UTC")
        val lastSuccessTime = Instant.ofEpochSecond(meta.lastSuccessTime).atZone(utcZone)
        val currentTime = ZonedDateTime.now(utcZone)

        return !((lastSuccessTime.dayOfMonth == currentTime.dayOfMonth &&
                (lastSuccessTime.hour >= 18 || currentTime.hour < 18)) ||

                (lastSuccessTime.plusDays(1L).dayOfMonth == currentTime.dayOfMonth &&
                        (lastSuccessTime.hour >= 18 && currentTime.hour < 18)))
    }

    private fun getCurrentTime(): Long {
        return ZonedDateTime.now(ZoneId.of("UTC"))
            .toEpochSecond()
    }

    private suspend fun getRemoteData(
        longitude: Double,
        latitude: Double,
        dateAtStartDay: ZonedDateTime
    ): List<UVIndexData> {
        val indices = api.get(latitude, longitude, dateAtStartDay).indices
        val daoItems = indices.map { item ->
            UVIndexData(
                time = item.dt.epochSecond,
                longitude = transformLongitudeToDb(longitude),
                latitude = transformLatitudeToDb(latitude),
                value = item.uvi
            )
        }
        return daoItems
    }

    companion object {
        private fun transformLongitudeToDb(longitude: Double): Int =
            (longitude * 2).roundToInt() * 5

        private fun transformLatitudeToDb(latitude: Double): Int = (latitude * 2).roundToInt() * 5
    }

}