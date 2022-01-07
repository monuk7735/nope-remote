package com.monuk7735.nope.remote

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.monuk7735.nope.remote.composables.*
import com.monuk7735.nope.remote.ui.theme.NopeRemoteTheme
import com.monuk7735.nope.remote.viewmodels.HomeActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
class HomeActivity : ComponentActivity() {

    private lateinit var viewModel: HomeActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(HomeActivityViewModel::class.java)

        setTheme(R.style.Theme_NopeRemote)
        setContent {
            NopeRemoteTheme {
                Root()
            }
        }
    }

    @Composable
    fun Root() {
        val (selectedTab, setSelectedTab) = remember {
            mutableStateOf(0)
        }
        val remotesListState = rememberLazyListState()
        val flowsListState = rememberLazyListState()
        Scaffold(
            modifier = Modifier.fillMaxWidth(),
            topBar = {
                AppBar(
                    title = stringResource(id = R.string.app_name)
                ) {
                    ActionButton(
                        name = "Settings",
                        icon = Icons.Outlined.Settings,
                        onClick = {
                            Intent(this@HomeActivity, SettingsActivity::class.java).run {
                                startActivity(this)
                            }
                        }
                    )
//                    OverFlowActionButton(
//                        name = "Test",
//                        icon = Icons.Outlined.MoreVert,
//                        onClick = {
//                            startActivity(it)
//                        },
//                        overFlowItems = mapOf(
//                            "Settings" to Intent(this@HomeActivity, SettingsActivity::class.java)
//                        )
//                    )
                }
            },
            content = { innerPadding ->
                val allRemotes = viewModel.allRemotesInfo.observeAsState(listOf()).value
                val allFlows = viewModel.allFlows.observeAsState(listOf()).value

                val vibrator = LocalContext.current.run {
                    getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }

                when (selectedTab) {
                    0 -> LazyColumn(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(horizontal = 2.dp)
                            .fillMaxSize(),
                        state = remotesListState,
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(allRemotes.size) { i ->
                            RemoteParent(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .fillMaxWidth()
                                    .height(80.dp),
                                name = allRemotes[i].name,
                                icon = allRemotes[i].getIcon(),
                                onClick = {
                                    startActivity(
                                        Intent(
                                            this@HomeActivity,
                                            RemoteControlActivity::class.java
                                        ).run {
                                            putExtra("remote", allRemotes[i])
                                        }
                                    )
                                }
                            )
                        }
                    }
                    1 -> LazyColumn(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(horizontal = 2.dp)
                            .fillMaxSize(),
                        state = flowsListState,
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(allFlows.size) { i ->
                            FlowParent(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .fillMaxWidth()
                                    .height(80.dp),
                                name = allFlows[i].name,
                                onExecute = {
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        allFlows[i].execute(
                                            viewModel.irController,
                                            vibrator
                                        )
                                    }
                                },
                                onEdit = {
                                    startActivity(
                                        Intent(
                                            this@HomeActivity,
                                            AddEditFlowActivity::class.java
                                        ).run {
                                            putExtra("flow_data", allFlows[i])
                                        }
                                    )
                                }
                            )
                        }
                        if (allFlows.isNotEmpty())
                            item {
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    text = "Tap Flow to transmit",
                                    textAlign = TextAlign.Center,
//                                    color = MaterialTheme.colors.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                    }
                }
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = {
                            setSelectedTab(0)
                        },
                        label = {
                            Text(
                                text = "Remotes",
//                                fontWeight = FontWeight.Bold
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_ir_remote),
                                contentDescription = "Remotes"
                            )
                        }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            setSelectedTab(1)
                        },
                        label = {
                            Text(
                                text = "Flows",
//                                fontWeight = FontWeight.Bold
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_ir_flow),
                                contentDescription = "Flows"
                            )
                        }
                    )
                }
            },
            floatingActionButton = {
                when (selectedTab) {
                    0 -> ExtendedFloatingActionButton(
                        text = {
                            Text(text = "Add Remote")
                        },
                        onClick = {
                            startActivity(
                                Intent(
                                    this@HomeActivity,
                                    AddRemoteActivity::class.java
                                )
                            )
                        },
                        icon = {
                            Icon(imageVector = Icons.Outlined.Add,
                                contentDescription = "Add Remote")
                        }
                    )
                    1 -> ExtendedFloatingActionButton(
                        text = {
                            Text(text = "Create Flow")
                        },
                        onClick = {
                            startActivity(
                                Intent(
                                    this@HomeActivity,
                                    AddEditFlowActivity::class.java
                                )
                            )
                        },
                        icon = {
                            Icon(imageVector = Icons.Outlined.Add,
                                contentDescription = "Create Flow")
                        }
                    )
                }
            }
        )
    }
}