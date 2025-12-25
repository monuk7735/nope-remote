package com.monuk7735.nope.remote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
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
import com.monuk7735.nope.remote.viewmodels.AddRemoteActivityViewModel

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
class AddRemoteActivity : ComponentActivity() {

    private lateinit var viewModel: AddRemoteActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[AddRemoteActivityViewModel::class.java]

        setContent {
            NopeRemoteTheme {
                AddRemoteActivityRoot()
            }
        }
    }

    @Composable
    fun AddRemoteActivityRoot() {
        val allTypes = viewModel.types.observeAsState().value
        val allBrands = viewModel.brands.observeAsState().value
        val allCodes = viewModel.codes.observeAsState().value

        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = AddRemoteNavigation.ListTypes.route
        ) {
            composable(
                route = AddRemoteNavigation.ListTypes.route
            ) {
                ListTypes(
                    allTypes = allTypes,
                    onOneClicked = { type ->
                        viewModel.getBrands(type)
                        navController.navigate(AddRemoteNavigation.ListBrands.route)
                    },
                    onSearch = { viewModel.filterTypes(it) },
                    onBack = {
                        finish()
                    }
                )
            }
            composable(
                route = AddRemoteNavigation.ListBrands.route
            ) {
                ListBrands(
                    allBrands = allBrands,
                    onOneClicked = { type, brand ->
                        viewModel.getCodes(type, brand)
                        navController.navigate(AddRemoteNavigation.ListCodes.route)
                    },
                    onSearch = { viewModel.filterBrands(it) },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = AddRemoteNavigation.ListCodes.route
            ) {
                ListCodes(
                    allCodes = allCodes,
                    onSave = {
                        viewModel.saveRemote(it)
                        finish()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}