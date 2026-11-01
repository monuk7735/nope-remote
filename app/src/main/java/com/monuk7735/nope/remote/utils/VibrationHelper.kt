package com.monuk7735.nope.remote.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.monuk7735.nope.remote.R

object VibrationHelper {

    fun shouldVibrate(context: Context): Boolean {
        val prefs = context.getSharedPreferences(
            context.getString(R.string.shared_pref_app_settings),
            Context.MODE_PRIVATE,
        )
        return prefs.getBoolean(
            context.getString(R.string.pref_settings_vibration),
            true,
        )
    }

    fun getVibrator(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    fun vibrate(context: Context, vibrator: Vibrator? = null, durationMs: Long = 100) {
        if (!shouldVibrate(context)) return
        val targetVibrator = vibrator ?: getVibrator(context) ?: return
        vibrate(targetVibrator, durationMs)
    }

    fun vibrate(vibrator: Vibrator, durationMs: Long = 100) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE),
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }
}
