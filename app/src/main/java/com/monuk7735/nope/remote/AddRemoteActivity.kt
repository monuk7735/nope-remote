package com.monuk7735.nope.remote


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.produceState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.monuk7735.nope.remote.composables.ListBrands
import com.monuk7735.nope.remote.composables.ListCodes
import com.monuk7735.nope.remote.composables.ListTypes
import com.monuk7735.nope.remote.navigation.AddRemoteNavigation
import com.monuk7735.nope.remote.ui.theme.NopeRemoteTheme
import com.monuk7735.nope.remote.ui.theme.rememberThemeSettings
import com.monuk7735.nope.remote.viewmodels.AddRemoteActivityViewModel

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
class AddRemoteActivity : ComponentActivity() {

    private lateinit var viewModel: AddRemoteActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[AddRemoteActivityViewModel::class.java]

        setContent {
            val themeSettings = rememberThemeSettings()
            NopeRemoteTheme(
                    useDarkTheme = themeSettings.useDarkTheme,
                    useDynamicColors = themeSettings.useDynamicColors
            ) { AddRemoteActivityRoot() }
        }
    }

    @Composable
    fun AddRemoteActivityRoot() {
        val allTypes = viewModel.types.observeAsState().value
        val allBrands = viewModel.brands.observeAsState().value
        val allCodes = viewModel.codes.observeAsState().value
        val loadingProgress = viewModel.loadingProgress.observeAsState().value

        val navController = rememberNavController()

        NavHost(
                navController = navController,
                startDestination = AddRemoteNavigation.ListTypes.route
        ) {
            composable(
                    route = AddRemoteNavigation.ListTypes.route,
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
                    }
            ) {
                val isRepoInstalled =
                        produceState(initialValue = true) {
                                    value = viewModel.isRepoInstalled()
                                }
                                .value

                val selectedRepoIndex = viewModel.selectedRepoIndex.observeAsState(0).value

                ListTypes(
                        allTypes = allTypes,
                        isRepoInstalled = isRepoInstalled,
                        availableRepos = viewModel.availableRepos,
                        selectedRepoIndex = selectedRepoIndex,
                        onRepoSelected = { viewModel.selectRepository(it) },
                        onOneClicked = { type ->
                            viewModel.getBrands(type)
                            navController.navigate(AddRemoteNavigation.ListBrands.route)
                        },
                        onSearch = { viewModel.filterTypes(it) },
                        onBack = { finish() },
                        onGoToSettings = {
                            startActivity(
                                    Intent(
                                            this@AddRemoteActivity,
                                            SettingsActivity::class.java
                                    )
                            )
                            finish()
                        }
                )
            }
            composable(
                    route = AddRemoteNavigation.ListBrands.route,
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
                    }
            ) {
                ListBrands(
                        allBrands = allBrands,
                        onOneClicked = { type, brand ->
                            viewModel.getCodes(type, brand)
                            navController.navigate(AddRemoteNavigation.ListCodes.route)
                        },
                        onSearch = { viewModel.filterBrands(it) },
                        onBack = { navController.popBackStack() }
                )
            }
            composable(
                    route = AddRemoteNavigation.ListCodes.route,
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
                        androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it })
                    }
            ) {
                ListCodes(
                        allCodes = allCodes,
                        loadingProgress = loadingProgress,
                        onSave = {
                            viewModel.saveRemote(it)
                            finish()
                        },
                        onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
