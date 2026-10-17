package com.monuk7735.nope.remote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.monuk7735.nope.remote.composables.AndroidTvRemoteScreen
import com.monuk7735.nope.remote.ui.theme.NopeRemoteTheme
import com.monuk7735.nope.remote.ui.theme.rememberThemeSettings

class AndroidTvRemoteActivity : ComponentActivity() {

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
                )
            }
        }
    }
}
