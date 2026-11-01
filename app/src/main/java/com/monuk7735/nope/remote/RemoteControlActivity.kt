package com.monuk7735.nope.remote

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.core.content.IntentCompat
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
import com.monuk7735.nope.remote.ui.theme.rememberThemeSettings
import com.monuk7735.nope.remote.utils.ShortcutHelper
import com.monuk7735.nope.remote.viewmodels.RemoteControlViewModel

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
)
class RemoteControlActivity : ComponentActivity() {

    lateinit var viewModel: RemoteControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(RemoteControlViewModel::class.java)

        setContent {
            val themeSettings = rememberThemeSettings()
            NopeRemoteTheme(
                useDarkTheme = themeSettings.useDarkTheme,
                useDynamicColors = themeSettings.useDynamicColors,
            ) { Root() }
        }
    }

    @Composable
    fun Root() {
        val initialRemote = remember {
            IntentCompat.getParcelableExtra(
                intent,
                "remote",
                RemoteDataDBModel::class.java,
            )
        }
        val remoteId = remember {
            val fromParcelable = initialRemote?.id ?: -1
            if (fromParcelable != -1) fromParcelable else intent.getIntExtra("remote_id", -1)
        }

        if (remoteId == -1) {
            finish()
            return
        }

        val liveRemote = viewModel.getLiveRemote(remoteId).observeAsState(initial = initialRemote)
        val currentRemote = liveRemote.value

        LaunchedEffect(remoteId) {
            if (initialRemote == null) {
                val remote = viewModel.getRemote(remoteId)
                if (remote == null) {
                    Toast.makeText(
                        this@RemoteControlActivity,
                        "Remote not found",
                        Toast.LENGTH_SHORT,
                    ).show()
                    finish()
                }
            }
        }

        if (currentRemote == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return
        }

        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = RemoteControlNavigation.RemoteControlMain.route,
        ) {
            composable(
                route = RemoteControlNavigation.RemoteControlMain.route,
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it })
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it })
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -it })
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { it })
                },
            ) {
                RemoteControl(
                    remoteDataModel = currentRemote,
                    onEditRemoteSettings = {
                        navController.navigate(
                            RemoteControlNavigation.RemoteControlSettings.route,
                        )
                    },
                    onEditLayout = {
                        navController.navigate(
                            RemoteControlNavigation.RemoteControlEditLayout.route,
                        )
                    },
                    onBack = { finish() },
                )
            }

            composable(
                route = RemoteControlNavigation.RemoteControlSettings.route,
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it })
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it })
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -it })
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { it })
                },
            ) {
                RemoteControlSettings(
                    remoteDataModel = currentRemote,
                    onSave = {
                        viewModel.updateRemote(it)
                        navController.popBackStack()
                    },
                    onEditLayout = {
                        navController.navigate(
                            RemoteControlNavigation.RemoteControlEditLayout.route,
                        )
                    },
                    onDelete = {
                        viewModel.deleteRemote(it)
                        ShortcutHelper.disableRemoteShortcut(this@RemoteControlActivity, it.id)
                        finish()
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = RemoteControlNavigation.RemoteControlEditLayout.route,
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it })
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it })
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -it })
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { it })
                },
            ) {
                RemoteControlEditLayout(
                    remoteDataModel = currentRemote,
                    onSaveLayout = {
                        viewModel.updateRemote(it)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
