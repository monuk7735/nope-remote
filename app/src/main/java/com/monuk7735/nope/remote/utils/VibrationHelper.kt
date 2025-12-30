package com.monuk7735.nope.remote.utils

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object VibrationHelper {
    fun vibrate(vibrator: Vibrator, durationMs: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }
}
