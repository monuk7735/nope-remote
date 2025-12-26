package com.monuk7735.nope.remote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.monuk7735.nope.remote.composables.AppBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import com.monuk7735.nope.remote.composables.SwitchPreference
import com.monuk7735.nope.remote.viewmodels.SettingsActivityViewModel
import com.monuk7735.nope.remote.ui.theme.NopeRemoteTheme
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp

@ExperimentalMaterial3Api
class SettingsActivity : ComponentActivity() {

    private lateinit var viewModel: SettingsActivityViewModel

    val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[SettingsActivityViewModel::class.java]

        setContent {
            val themeSettings = com.monuk7735.nope.remote.ui.theme.rememberThemeSettings()
            NopeRemoteTheme(
                useDarkTheme = themeSettings.useDarkTheme,
                useDynamicColors = themeSettings.useDynamicColors
            ) {
                SettingsRoot()
            }
        }
    }

    @Composable
    fun SettingsRoot() {
        val vibrate by viewModel.vibrateSettingsValue.observeAsState(true)
        val darkMode by viewModel.darkModeSettingsValue.observeAsState(0)
        val dynamicColor by viewModel.dynamicColorSettingsValue.observeAsState(true)

        NopeRemoteTheme(
            useDarkTheme = when (darkMode) {
                1 -> false // Light
                2 -> true // Dark
                else -> isSystemInDarkTheme() // System
            },
            useDynamicColors = dynamicColor
        ) {
            Scaffold(
                topBar = {
                    AppBar(
                        title = "Settings",
                        onBack = { finish() }
                    )
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            ) { padding ->
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        com.monuk7735.nope.remote.composables.SettingsGroup(title = "General") {
                            SwitchPreference(
                                title = "Vibrate",
                                summary = "Vibrate on button press",
                                icon = Icons.Outlined.Vibration,
                                value = vibrate,
                                onValueChange = {
                                    viewModel.vibrateSettingsValue.value = it
                                    viewModel.saveSettings(applicationContext)
                                }
                            )
                        }
                    }

                    item {
                        com.monuk7735.nope.remote.composables.SettingsGroup(title = "Appearance") {
                            val darkModeOptions = listOf("System Default", "Light", "Dark")
                            com.monuk7735.nope.remote.composables.SingleChoicePreference(
                                title = "Dark Mode",
                                summary = darkModeOptions.getOrElse(darkMode) { "System Default" },
                                icon = Icons.Outlined.Brightness4,
                                options = darkModeOptions,
                                selectedOption = darkMode,
                                onOptionSelected = {
                                    viewModel.darkModeSettingsValue.value = it
                                    viewModel.saveSettings(applicationContext)
                                }
                            )
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                SwitchPreference(
                                    title = "Dynamic Color",
                                    summary = "Use wallpaper colors",
                                    icon = Icons.Outlined.Palette,
                                    value = dynamicColor,
                                    onValueChange = {
                                        viewModel.dynamicColorSettingsValue.value = it
                                        viewModel.saveSettings(applicationContext)
                                    }
                                )
                            }
                        }
                    }

                    item {
                        com.monuk7735.nope.remote.composables.SettingsGroup(title = "About") {
                            com.monuk7735.nope.remote.composables.InfoPreference(
                                title = "App Version",
                                value = BuildConfig.VERSION_NAME,
                                icon = Icons.Outlined.Info
                            )
                        }
                    }
                }
            }
        }
    }
}