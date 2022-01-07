package com.monuk7735.nope.remote.infrared

import android.hardware.ConsumerIrManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.monuk7735.nope.remote.infrared.patterns.IRPattern


class IRController(private val consumerIrManager: ConsumerIrManager) {

    fun transmit(irPattern: IRPattern, vibrator: Vibrator) {
        transmit(irPattern)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(100)
        }
    }

    fun transmit(irPattern: IRPattern) {
        consumerIrManager.transmit(irPattern.frequency, irPattern.intervals)
    }
}