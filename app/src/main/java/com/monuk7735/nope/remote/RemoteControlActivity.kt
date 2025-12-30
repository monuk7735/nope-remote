package com.monuk7735.nope.remote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
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
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(RemoteControlViewModel::class.java)

        //        val remote = intent.getParcelableExtra<RemoteDataDBModel>("remote")
        //
        //        if (remote == null) {
        //            finish()
        //            return
        //        }

        setContent {
            val themeSettings = com.monuk7735.nope.remote.ui.theme.rememberThemeSettings()
            NopeRemoteTheme(
                    useDarkTheme = themeSettings.useDarkTheme,
                    useDynamicColors = themeSettings.useDynamicColors
            ) { Root() }
        }
    }

    @Composable
    fun Root() {

        val (remoteData, setRemoteData) =
                remember { mutableStateOf(intent.getParcelableExtra<RemoteDataDBModel>("remote")) }

        if (remoteData == null) {
            finish()
            return
        }

        val liveRemote = viewModel.getLiveRemote(remoteData.id).observeAsState(initial = remoteData)
        val navController = rememberNavController()

        NavHost(
                navController = navController,
                startDestination = RemoteControlNavigation.RemoteControlMain.route
        ) {
            composable(
                    route = RemoteControlNavigation.RemoteControlMain.route,
                    enterTransition = {
                        androidx.compose.animation.slideInHorizontally(initialOffsetX = { it })
                    },
                    exitTransition = {
                        androidx.compose.animation.slideOutHorizontally(targetOffsetX = { -it })
                    },
                    popEnterTransition = {
                        androidx.compose.animation.slideInHorizontally(initialOffsetX = { -it })
                    },
                    popExitTransition = {
                        androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it })
                    }
            ) {
                RemoteControl(
                        remoteDataModel = liveRemote.value,
                        onEditRemoteSettings = {
                            navController.navigate(
                                    RemoteControlNavigation.RemoteControlSettings.route
                            )
                        },
                        onBack = { finish() }
                )
            }

            composable(
                    route = RemoteControlNavigation.RemoteControlSettings.route,
                    enterTransition = {
                        androidx.compose.animation.slideInHorizontally(initialOffsetX = { it })
                    },
                    exitTransition = {
                        androidx.compose.animation.slideOutHorizontally(targetOffsetX = { -it })
                    },
                    popEnterTransition = {
                        androidx.compose.animation.slideInHorizontally(initialOffsetX = { -it })
                    },
                    popExitTransition = {
                        androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it })
                    }
            ) {
                RemoteControlSettings(
                        remoteDataModel = liveRemote.value,
                        onSave = {
                            viewModel.updateRemote(it)
                            navController.popBackStack()
                        },
                        onEditLayout = {
                            setRemoteData(intent.getParcelableExtra("remote"))
                            navController.navigate(
                                    RemoteControlNavigation.RemoteControlEditLayout.route
                            )
                        },
                        onDelete = {
                            viewModel.deleteRemote(it)
                            finish()
                        },
                        onBack = { navController.popBackStack() }
                )
            }

            composable(
                    route = RemoteControlNavigation.RemoteControlEditLayout.route,
                    enterTransition = {
                        androidx.compose.animation.slideInHorizontally(initialOffsetX = { it })
                    },
                    exitTransition = {
                        androidx.compose.animation.slideOutHorizontally(targetOffsetX = { -it })
                    },
                    popEnterTransition = {
                        androidx.compose.animation.slideInHorizontally(initialOffsetX = { -it })
                    },
                    popExitTransition = {
                        androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it })
                    }
            ) {
                RemoteControlEditLayout(
                        remoteDataModel = remoteData,
                        onSaveLayout = {
                            viewModel.updateRemote(it)
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
