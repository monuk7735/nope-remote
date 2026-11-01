package com.monuk7735.nope.remote.infrared

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Vibrator
import com.monuk7735.nope.remote.infrared.patterns.IRPattern
import com.monuk7735.nope.remote.utils.VibrationHelper

class IRController(
    private val consumerIrManager: ConsumerIrManager,
    private val context: Context? = null,
) {

    fun transmit(irPattern: IRPattern, vibrator: Vibrator? = null) {
        consumerIrManager.transmit(irPattern.frequency, irPattern.intervals)

        if (context != null) {
            VibrationHelper.vibrate(context, vibrator, 100)
        } else if (vibrator != null) {
            VibrationHelper.vibrate(vibrator, 100)
        }
    }
}
