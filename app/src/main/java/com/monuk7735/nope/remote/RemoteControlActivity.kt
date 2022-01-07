package com.monuk7735.nope.remote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.monuk7735.nope.remote.composables.RemoteControl
import com.monuk7735.nope.remote.composables.RemoteControlEditLayout
import com.monuk7735.nope.remote.composables.RemoteControlSettings
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.navigation.RemoteControlNavigation
import com.monuk7735.nope.remote.ui.theme.NopeRemoteTheme
import com.monuk7735.nope.remote.viewmodels.RemoteControlViewModel

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
class RemoteControlActivity : ComponentActivity() {

    lateinit var viewModel: RemoteControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(RemoteControlViewModel::class.java)

//        val remote = intent.getParcelableExtra<RemoteDataDBModel>("remote")
//
//        if (remote == null) {
//            finish()
//            return
//        }

        setContent {
            NopeRemoteTheme {
                Root()
            }
        }
    }

    @Composable
    fun Root() {

        val (remoteData, setRemoteData) = remember {
            mutableStateOf(
                intent.getParcelableExtra<RemoteDataDBModel>("remote")
            )
        }

        if (remoteData == null) {
            finish()
            return
        }

        var liveRemote: RemoteDataDBModel? = remember {
            remoteData
        }

        val navController = rememberNavController()

        viewModel.getLiveRemote(remoteData.id).observe(this) {
            liveRemote = it
        }

        NavHost(
            navController = navController,
            startDestination = RemoteControlNavigation.RemoteControlMain.route
        ) {

            composable(
                route = RemoteControlNavigation.RemoteControlMain.route,
            ) {
                RemoteControl(
                    remoteDataModel = liveRemote,
                    onEditRemoteSettings = {
                        navController.navigate(RemoteControlNavigation.RemoteControlSettings.route)
                    }
                )
            }

            composable(
                route = RemoteControlNavigation.RemoteControlSettings.route,
            ) {
                RemoteControlSettings(
                    remoteDataModel = liveRemote,
                    onSave = {
                        viewModel.updateRemote(it)
                        navController.popBackStack()
                    },
                    onEditLayout = {
                        setRemoteData(intent.getParcelableExtra("remote"))
                        navController.navigate(RemoteControlNavigation.RemoteControlEditLayout.route)
                    },
                    onDelete = {
                        viewModel.deleteRemote(it)
                        finish()
                    }
                )
            }

            composable(
                route = RemoteControlNavigation.RemoteControlEditLayout.route,
            ) {
                RemoteControlEditLayout(
                    remoteDataModel = remoteData,
                    onSaveLayout = {
                        viewModel.updateRemote(it)
                        navController.popBackStack()
                    }
                )
            }

        }
    }
}