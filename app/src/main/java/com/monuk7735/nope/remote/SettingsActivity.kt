package com.monuk7735.nope.remote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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

    lateinit var viewModel: SettingsActivityViewModel

    val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(SettingsActivityViewModel::class.java)

        setContent {
            NopeRemoteTheme {
                SettingsRoot()
            }
        }
    }

    @Composable
    fun SettingsRoot() {

//        var vibrationState by remember {
//            mutableStateOf(true)
//        }

        Scaffold(
            topBar = {
                AppBar(
                    title = "Settings"
                )
            },
            content = {
                Column {
                    SwitchPreference(
                        title = "Vibrate",
                        summary = "Vibrate on touch",
                        value = viewModel.vibrateSettingsValue.observeAsState().value?:true,
                        onValueChange = {
//                            vibrationState = it
                            viewModel.vibrateSettingsValue.value = it
                            viewModel.saveSettings(applicationContext)
                        }
                    )
                }
            }
        )
    }

//    @Preview(showBackground = true)
//    @Composable
//    fun DefaultPreview() {
//        NopeRemoteTheme {
//            SettingsRoot()
//        }
//    }
}