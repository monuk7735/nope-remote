package com.monuk7735.nope.remote.models.custom.macros

import android.os.Parcelable
import com.monuk7735.nope.remote.infrared.patterns.IRPattern
import kotlinx.parcelize.Parcelize

@Parcelize
data class MacroTransmit(
    var name: String,
    val sourceRemoteId:Int,
    val sourceButtonName: String,
    val irPattern: IRPattern
) : Parcelable
