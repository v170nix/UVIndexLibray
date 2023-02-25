package uv.index.lib.data

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class UVSummaryDayData(
    val day: LocalDate,
    val maxIndex: UVIndexData,
    val hours: List<UVIndexData> ? = null,
    val timeProtectionBegin: LocalTime? = null,
    val timeProtectionEnd: LocalTime? = null
) {
    companion object {
        fun createFromDayList(zoneId: ZoneId, list: List<UVIndexData>): UVSummaryDayData? {
            if (list.size < 23) return null
            val times = list.getSunProtectionPeriod()
            return UVSummaryDayData(
                day = Instant.ofEpochSecond(list.first().time).atZone(zoneId).toLocalDate(),
                maxIndex = list.maxByOrNull { it.value }!!,
                timeProtectionBegin = times?.first,
                timeProtectionEnd = times?.second,
                hours = list
            )
        }
    }
}