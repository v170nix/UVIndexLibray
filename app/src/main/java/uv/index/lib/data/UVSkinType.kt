@file:Suppress("unused")

package uv.index.lib.data

import kotlin.math.roundToInt

// https://dermnetnz.org/topics/sunburn

enum class UVSkinType(
    private val minMaxMed: Pair<Int, Int>
) {
    Type1(15 to 30),
    Type2(25 to 40),
    Type3(30 to 50),
    Type4(40 to 60),
    Type5(60 to 90),
    Type6(90 to 150);


//    fun getTimeToBurnInMins(uvIndex: Double): Double {
//        return (minMaxMed.first + minMaxMed.second) / 2.0 * factor / uvIndex
//    }

    fun getMinTimeToBurnInMins(uvIndex: Double): Double {
        return minMaxMed.first * factor / uvIndex
    }

    fun getMaxTimeToBurnInMins(uvIndex: Double): Double {
        return minMaxMed.second * factor / uvIndex
    }

    fun getIntegralMinTimeToBurnInMins(list: List<UVIndexData>, currentHour: Double): Double? {
        return getIntegralTimeInMins(this.minMaxMed.first, list, currentHour)
    }

    fun getIntegralMaxTimeToBurnInMins(list: List<UVIndexData>, currentHour: Double): Double? {
        return getIntegralTimeInMins(this.minMaxMed.second, list, currentHour)
    }

    fun getIntegralMinTimeToVitaminDInMins(list: List<UVIndexData>, currentHour: Double): Double? {
        return getIntegralTimeInMins((this.minMaxMed.first * 0.3).roundToInt(), list, currentHour)
    }

    fun getIntegralMaxTimeToVitaminDInMins(list: List<UVIndexData>, currentHour: Double): Double? {
        return getIntegralTimeInMins((this.minMaxMed.second * 0.3).roundToInt(), list, currentHour)
    }

    private companion object {
        private const val factor = 1e-4 / (1 / 40.0 * 1e-5) / 60.0

        private fun getIntegralTimeInMins(
            skinJ: Int,
            list: List<UVIndexData>,
            currentHour: Double
        ): Double? {
            val minInHour = 1.0 / 60.0
            var timeSum = 0.0
            var j = 0.0
            while (j < skinJ * 1e-3) {
                if (currentHour + timeSum >= 24.0) return null
                val index = list.getCurrentIndex(currentHour + timeSum)
                j += index / 40.0 * 1e-4 * 60.0
                timeSum += minInHour
            }
            return timeSum * 60.0
        }
    }


}