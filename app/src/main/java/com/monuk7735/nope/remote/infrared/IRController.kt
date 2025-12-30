package com.monuk7735.nope.remote.infrared

import android.hardware.ConsumerIrManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.monuk7735.nope.remote.infrared.patterns.IRPattern
import com.monuk7735.nope.remote.utils.VibrationHelper


class IRController(private val consumerIrManager: ConsumerIrManager) {

    fun transmit(irPattern: IRPattern, vibrator: Vibrator) {
        transmit(irPattern)

        VibrationHelper.vibrate(vibrator, 100)
    }

    fun transmit(irPattern: IRPattern) {
        consumerIrManager.transmit(irPattern.frequency, irPattern.intervals)
    }
}