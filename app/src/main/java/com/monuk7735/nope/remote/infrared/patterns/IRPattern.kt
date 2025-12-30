package com.monuk7735.nope.remote.infrared.patterns

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class IRPattern(val irPatternType: IRPatternType, val frequency: Int, val data: IntArray) : Parcelable {

    @IgnoredOnParcel
    var intervals = IntArray(data.size)
    @IgnoredOnParcel
    var cycles = IntArray(data.size)

    init {
        if (irPatternType == IRPatternType.Cycles) {
            cycles = data
            intervals = convertToIntervals()
        } else if (irPatternType == IRPatternType.Intervals) {
            intervals = data
            cycles = convertToCycles()
        }
    }

    private fun convertToCycles(): IntArray {
        val cycles = IntArray(intervals.size)

        val k = 1000000.0 / frequency

        for (i in cycles.indices) {
            cycles[i] = (intervals[i] / k).toInt()
        }

        return cycles
    }

    private fun convertToIntervals(): IntArray {
        val intervals = IntArray(cycles.size)

        val k = 1000000.0 / frequency

        for (i in intervals.indices) {
            intervals[i] = (cycles[i] * k).toInt()
        }

        return intervals
    }
}