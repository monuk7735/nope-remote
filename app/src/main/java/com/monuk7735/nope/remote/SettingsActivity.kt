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
import com.monuk7735.nope.remote.composables.SwitchPreference
import com.monuk7735.nope.remote.ui.theme.NopeRemoteTheme
import com.monuk7735.nope.remote.viewmodels.SettingsActivityViewModel

@ExperimentalMaterial3Api
class SettingsActivity : ComponentActivity() {

    private lateinit var viewModel: SettingsActivityViewModel

    val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[SettingsActivityViewModel::class.java]

        setContent {
            NopeRemoteTheme {
                SettingsRoot()
            }
        }
    }

    @Composable
    fun SettingsRoot() {
        Scaffold(
            topBar = {
                AppBar(
                    title = "Settings"
                )
            },
            content = { padding ->
                Column(modifier = Modifier.padding(padding)) {
                    SwitchPreference(
                        title = "Vibrate",
                        summary = "Vibrate on touch",
                        value = viewModel.vibrateSettingsValue.observeAsState().value ?: true,
                        onValueChange = {
                            viewModel.vibrateSettingsValue.value = it
                            viewModel.saveSettings(applicationContext)
                        }
                    )
                }
            }
        )
    }
}