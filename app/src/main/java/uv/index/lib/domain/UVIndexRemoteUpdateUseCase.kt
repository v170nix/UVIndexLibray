@file:Suppress("unused")

package uv.index.lib.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.arwix.extension.ConflatedJob
import uv.index.lib.data.UVIPlaceData
import uv.index.lib.data.UVIndexData
import uv.index.lib.data.UVIndexRepository
import java.time.LocalDate

class UVIndexRemoteUpdateUseCase(
    private val repository: UVIndexRepository
) {

    private val _flow =
        MutableStateFlow<UVIndexRepository.RemoteUpdateState>(UVIndexRepository.RemoteUpdateState.None)
    private val workJob = ConflatedJob()
    private val checkJob = ConflatedJob()

    val asFlow = _flow.asStateFlow()

    private fun cancelUpdate() {
        checkJob.cancel()
        workJob.cancel()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun update(
        scope: CoroutineScope,
        place: UVIPlaceData?) {
        if (place == null) {
            cancelUpdate()
            return
        }
        val dateAtStartDay = LocalDate.now(place.zone).atStartOfDay(place.zone)
        workJob += repository
            .updateFromRemoteAsFlow(
                place.longitude,
                place.latitude,
                dateAtStartDay
            )
            .onEach { state -> _flow.update { state } }
            .launchIn(scope)

    }

    fun checkAndUpdate(
        scope: CoroutineScope,
        place: UVIPlaceData?,
        currentDayData: List<UVIndexData>?,
    ) {
        if (place == null) {
            cancelUpdate()
            return
        }

        checkJob += scope.launch {
            val doUpdate = repository.checkForRemoteUpdateData(
                place.longitude,
                place.latitude
            ) || (currentDayData?.let { it.size < 23 } ?: true)
            if (doUpdate) update(this, place)
        }

    }


}