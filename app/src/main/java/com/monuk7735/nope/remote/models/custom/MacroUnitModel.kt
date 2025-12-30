package com.monuk7735.nope.remote.models.custom

import com.monuk7735.nope.remote.infrared.patterns.IRPattern

class MacroUnitModel(
    val unitName:String,
    val irPattern: IRPattern
) {

    fun execute(){
    }

    fun getName(): String {
        return  ""
    }

}
