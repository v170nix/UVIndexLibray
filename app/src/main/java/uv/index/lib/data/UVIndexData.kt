@file:Suppress("unused")

package uv.index.lib.data

import androidx.room.Entity
import androidx.room.Ignore
import java.time.LocalTime
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Entity(tableName = "uv_index", primaryKeys = ["longitude", "latitude", "time"])
data class UVIndexData(
    val time: Long,
    val longitude: Int,
    val latitude: Int,
    val value: Double,
) {

    @Ignore
    fun getIntIndex() = value.roundToInt()
}

fun List<UVIndexData>.getCurrentIndex(hour: Double): Double {
    val intPart = hour.toInt()
    val fractionalPart = hour - intPart
    val prev = this[intPart]
    val next = if (intPart < 23) this[intPart + 1] else prev
    val delta = next.value - prev.value
    return prev.value + delta * fractionalPart
}

fun List<UVIndexData>.getSunProtectionPeriod(): Pair<LocalTime, LocalTime>? {
    var start: UVIndexData? = null
    var end: UVIndexData? = null
    forEach {
        if (it.value >= 3 && start == null) {
            start = it
        }
        if (it.value < 3 && start != null && end == null) {
            end = it
            return@forEach
        }
    }
    if (start == null || end == null) return null
    val startIndex = indexOf(start)
    val startHour: Double = if (startIndex > 0) {
        val delta = this[startIndex].value - this[startIndex - 1].value
        startIndex - (3.0 - this[startIndex - 1].value) / delta
    } else startIndex.toDouble()

    val endIndex = indexOf(end)
    val endHour: Double = if (endIndex > 0) {
        val delta = this[endIndex - 1].value - this[endIndex].value
        endIndex - (3.0 - this[endIndex].value) / delta
    } else endIndex.toDouble()

    return LocalTime.ofSecondOfDay((startHour * 60.0 * 60.0).roundToLong()) to
            LocalTime.ofSecondOfDay((endHour * 60.0 * 60.0).roundToLong())
}