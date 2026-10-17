package com.monuk7735.nope.remote

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.monuk7735.nope.remote.composables.AndroidTvRemoteScreen
import com.monuk7735.nope.remote.ui.theme.NopeRemoteTheme
import com.monuk7735.nope.remote.ui.theme.rememberThemeSettings

class AndroidTvRemoteActivity : ComponentActivity() {

    private var volumeKeyHandler: ((KeyEvent) -> Boolean)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val host = intent.getStringExtra("host") ?: run {
            finish()
            return
        }
        val name = intent.getStringExtra("name") ?: "Android TV"

        setContent {
            val themeSettings = rememberThemeSettings()
            NopeRemoteTheme(
                useDarkTheme = themeSettings.useDarkTheme,
                useDynamicColors = themeSettings.useDynamicColors,
            ) {
                AndroidTvRemoteScreen(
                    host = host,
                    deviceName = name,
                    onBack = { finish() },
                    onRegisterVolumeHandler = { volumeKeyHandler = it },
                )
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        ) {
            if (volumeKeyHandler?.invoke(event) == true) {
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
