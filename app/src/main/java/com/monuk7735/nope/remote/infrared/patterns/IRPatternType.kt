package com.monuk7735.nope.remote.infrared.patterns

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class IRPatternType : Parcelable {
    Cycles,
    Intervals
}