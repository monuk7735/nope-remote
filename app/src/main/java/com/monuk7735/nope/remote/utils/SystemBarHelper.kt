package com.monuk7735.nope.remote.utils

import android.graphics.Color
import android.os.Build
import android.view.Window

object SystemBarHelper {
    fun setTransparentSystemBars(window: Window) {
        if (Build.VERSION.SDK_INT < 35) {
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.TRANSPARENT
        }
    }
}
