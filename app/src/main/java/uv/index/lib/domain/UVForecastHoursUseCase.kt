package uv.index.lib.domain

import kotlinx.coroutines.flow.*
import uv.index.lib.data.UVIndexData
import uv.index.lib.data.UVIndexRepository
import java.time.ZonedDateTime

class UVForecastHoursUseCase(
    private val repository: UVIndexRepository
) {
    suspend operator fun invoke(
        longitude: Double,
        latitude: Double,
        currentDateAtStartDay: ZonedDateTime,
        count: Long = 2L
    ): List<UVIndexData> {
        return (0L until count)
            .asFlow()
            .map { currentDateAtStartDay.plusDays(it) }
            .map {
                repository.getDataAsFlow(
                    longitude,
                    latitude,
                    it
                ).firstOrNull()
            }
            .filterNotNull()
            .filter { it.isNotEmpty() }
            .fold(mutableListOf()) { acc, value ->
                acc.addAll(value)
                acc
            }
    }

}