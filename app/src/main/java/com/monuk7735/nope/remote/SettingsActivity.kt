package com.monuk7735.nope.remote


import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.Abc
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider

import com.monuk7735.nope.remote.composables.AppBar
import com.monuk7735.nope.remote.composables.InfoPreference
import com.monuk7735.nope.remote.composables.LogOutputView
import com.monuk7735.nope.remote.composables.RepositoryPreference
import com.monuk7735.nope.remote.composables.SettingsGroup
import com.monuk7735.nope.remote.composables.SingleChoicePreference
import com.monuk7735.nope.remote.composables.SwitchPreference
import com.monuk7735.nope.remote.ui.theme.NopeRemoteTheme
import com.monuk7735.nope.remote.ui.theme.rememberThemeSettings
import com.monuk7735.nope.remote.viewmodels.SettingsActivityViewModel

@ExperimentalMaterial3Api
class SettingsActivity : ComponentActivity() {

    private lateinit var viewModel: SettingsActivityViewModel

    val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[SettingsActivityViewModel::class.java]

        setContent {
            val themeSettings = rememberThemeSettings()
            NopeRemoteTheme(
                    useDarkTheme = themeSettings.useDarkTheme,
                    useDynamicColors = themeSettings.useDynamicColors
            ) { SettingsRoot() }
        }
    }

    @Composable
    fun SettingsRoot() {
        val vibrate by viewModel.vibrateSettingsValue.observeAsState(true)
        val darkMode by viewModel.darkModeSettingsValue.observeAsState(0)
        val dynamicColor by viewModel.dynamicColorSettingsValue.observeAsState(true)

        val commandOutput by viewModel.repoCommandOutput.observeAsState("")
        val isDevMode by viewModel.devModeSettingsValue.observeAsState(false)

        var showLogs by remember { mutableStateOf(false) }
        val uriHandler = LocalUriHandler.current

        // Simple effect to show toast when dev mode becomes enabled
        // In a real app we might use a dedicated event flow, but this works for simple state
        var previousDevMode by remember { mutableStateOf(isDevMode) }
        if (isDevMode && !previousDevMode) {
             android.widget.Toast.makeText(LocalContext.current, "Developer Mode Enabled", android.widget.Toast.LENGTH_SHORT).show()
             previousDevMode = true
        } else if (!isDevMode) {
            previousDevMode = false
        }

        NopeRemoteTheme(
                useDarkTheme =
                        when (darkMode) {
                            1 -> false
                            2 -> true
                            else -> isSystemInDarkTheme()
                        },
                useDynamicColors = dynamicColor
        ) {
            Scaffold(
                    topBar = { AppBar(title = "Settings", onBack = { finish() }) },
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
            ) { padding ->
                LazyColumn(
                        modifier =
                                Modifier.padding(padding)
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.background),
                        contentPadding =
                                PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        SettingsGroup(title = "General") {
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
                        SettingsGroup(title = "Appearance") {
                            val darkModeOptions = listOf("System Default", "Light", "Dark")
                            SingleChoicePreference(
                                    title = "Dark Mode",
                                    summary =
                                            darkModeOptions.getOrElse(darkMode) {
                                                "System Default"
                                            },
                                    icon = Icons.Outlined.Brightness4,
                                    options = darkModeOptions,
                                    selectedOption = darkMode,
                                    onOptionSelected = {
                                        viewModel.darkModeSettingsValue.value = it
                                        viewModel.saveSettings(applicationContext)
                                    }
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                            ) {
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
                            SwitchPreference(
                                title = "Prefer Custom UI (Beta)",
                                summary = "Use specialized layouts for Remotes if available",
                                icon = Icons.Outlined.Palette,
                                value = viewModel.preferCustomUiGlobal.observeAsState(true).value,
                                onValueChange = {
                                    viewModel.preferCustomUiGlobal.value = it
                                    viewModel.saveSettings(applicationContext)
                                }
                            )

                            SwitchPreference(
                                    title = "Text Only Buttons",
                                    summary = "Hide icons and show text labels on buttons",
                                    icon = Icons.Outlined.Abc,
                                    value = viewModel.textButtonsOnlySettingsValue.observeAsState(false).value,
                                    onValueChange = {
                                        viewModel.textButtonsOnlySettingsValue.value = it
                                        viewModel.saveSettings(applicationContext)
                                    }
                            )
                        }
                    }

                    item {
                            SettingsGroup(
                                    title = "Repositories"
                            ) {
                            val repoStates by viewModel.repoStates.observeAsState(emptyMap())
                            
                            viewModel.availableRepositories.forEach { repo ->
                                val repoState = repoStates[repo.directoryName] ?: com.monuk7735.nope.remote.service.RepoState()
                                val isInstalled = viewModel.isRepoInstalled(repo.directoryName)
                                
                                RepositoryPreference(
                                        name = repo.title,
                                        url = repo.displayUrl,
                                        isInstalled = isInstalled,
                                        state = repoState,
                                        onDownload = { viewModel.manageRepository(repo.url, repo.title, repo.directoryName) },
                                        onDelete = { viewModel.deleteRepository(repo.directoryName) },
                                        onUrlClick = { uriHandler.openUri(repo.displayUrl) }
                                )
                            }
                            
                            
                            if (isDevMode) {
                                TextButton(
                                    onClick = { showLogs = !showLogs },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Text(if (showLogs) "Hide Logs" else "Show Logs")
                                }
                            }
                        }
                    }

                    item {
                        SettingsGroup(title = "About") {
                             val context = LocalContext.current
                             // We'll wrap the InfoPreference in a Box or Row to add the click listener for the whole item
                             Box(modifier = Modifier.clickable { viewModel.onVersionClicked() }) {
                                 InfoPreference(
                                         title = "App Version",
                                         value = BuildConfig.VERSION_NAME,
                                         icon = Icons.Outlined.Info
                                 )
                             }
                        }
                    }
                }
                
                if (showLogs) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        LogOutputView(output = commandOutput)
                    }
                }
            }
        }
    }
}
