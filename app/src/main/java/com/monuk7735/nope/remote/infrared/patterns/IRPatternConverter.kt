package com.monuk7735.nope.remote.infrared.patterns

class IRPatternConverter (){

    fun count2duration(frequency: Int, countPattern: IntArray): IntArray {

        val pulses = 1000000 / frequency

        val toRet: ArrayList<Int> = ArrayList()

        for (i in countPattern) {
            val duration = i * pulses
            toRet.add(duration)
        }

        return toRet.toIntArray()
    }

}