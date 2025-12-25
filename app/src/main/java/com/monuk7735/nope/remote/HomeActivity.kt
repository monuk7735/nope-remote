package com.monuk7735.nope.remote

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.monuk7735.nope.remote.composables.ActionButton
import com.monuk7735.nope.remote.composables.AppBar
import com.monuk7735.nope.remote.composables.FlowParent
import com.monuk7735.nope.remote.composables.RemoteParent
import com.monuk7735.nope.remote.ui.theme.NopeRemoteTheme
import com.monuk7735.nope.remote.viewmodels.HomeActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@OptIn(ExperimentalMaterial3Api::class)
class HomeActivity : ComponentActivity() {

    private lateinit var viewModel: HomeActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[HomeActivityViewModel::class.java]

        setContent {
            NopeRemoteTheme {
                Root()
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
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
                                    .padding(3.dp)
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
                                    .padding(3.dp)
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
                                    fontWeight = FontWeight.Bold
                                )
                            }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = {
                            setSelectedTab(0)
                        },
                        label = {
                            Text(
                                text = "Remotes",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp
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
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_ir_flow),
                                contentDescription = "Flows"
                            )
                        },
                    )
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = {
                        Text(text = if (selectedTab == 0) "Add Remote" else "Add Flow")
                    },
                    onClick = {
                        startActivity(
                            Intent(
                                this@HomeActivity,
                                if (selectedTab == 0) AddRemoteActivity::class.java else AddEditFlowActivity::class.java
                            )
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = if (selectedTab == 0) "Add Remote" else "Add Flow"
                        )
                    }
                )
            }
        )
    }
}