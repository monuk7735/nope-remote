package com.monuk7735.nope.remote

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.monuk7735.nope.remote.composables.EmptyState
import com.monuk7735.nope.remote.composables.MacroSequenceCard
import com.monuk7735.nope.remote.composables.RemoteTile
import com.monuk7735.nope.remote.ui.theme.NopeRemoteTheme
import com.monuk7735.nope.remote.viewmodels.HomeActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
class HomeActivity : ComponentActivity() {

    private lateinit var viewModel: HomeActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[HomeActivityViewModel::class.java]

        setContent {
            val themeSettings = com.monuk7735.nope.remote.ui.theme.rememberThemeSettings()
            NopeRemoteTheme(
                useDarkTheme = themeSettings.useDarkTheme,
                useDynamicColors = themeSettings.useDynamicColors
            ) {
                Root()
            }
        }
    }

    @Composable
    fun Root() {
        val pagerState = rememberPagerState(pageCount = { 2 })
        val coroutineScope = rememberCoroutineScope()
        
        val remotesGridState = rememberLazyGridState()
        val macrosListState = rememberLazyListState()

        val isFabExpanded by remember {
            derivedStateOf {
                if (pagerState.currentPage == 0) {
                    remotesGridState.firstVisibleItemIndex == 0
                } else {
                    macrosListState.firstVisibleItemIndex == 0
                }
            }
        }
        
        val context = LocalContext.current
        val allRemotes by viewModel.allRemotesInfo.observeAsState(emptyList())
        val allMacros by viewModel.allMacros.observeAsState(emptyList())

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                AppBar(
                    title = stringResource(id = R.string.app_name)
                ) {
                    Surface(
                        onClick = {
                            startActivity(Intent(context, SettingsActivity::class.java))
                        },
                        shape = CircleShape,
                        color = Color.Transparent
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(84.dp)
                ) {
                    NavigationBarItem(
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        },
                        label = { 
                            Text(
                                "Remotes",
                                fontWeight = if (pagerState.currentPage == 0) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_ir_remote),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    )
                    NavigationBarItem(
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(1) }
                        },
                        label = { 
                            Text(
                                "Macros",
                                fontWeight = if (pagerState.currentPage == 1) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_ir_flow),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    )
                }
            },
            floatingActionButton = {
                val fabColor = MaterialTheme.colorScheme.primary
                
                Surface(
                    onClick = {
                        val intent = if (pagerState.currentPage == 0) {
                            Intent(context, AddRemoteActivity::class.java)
                        } else {
                            Intent(context, AddEditMacroActivity::class.java)
                        }
                        startActivity(intent)
                    },
                    shape = RoundedCornerShape(24.dp),
                    color = fabColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = if (isFabExpanded) 24.dp else 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isFabExpanded,
                            enter = androidx.compose.animation.expandHorizontally() + androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.shrinkHorizontally() + androidx.compose.animation.fadeOut()
                        ) {
                            Row {
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (pagerState.currentPage == 0) "Add Remote" else "Add Macro",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            val vibrator = remember {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) { pageIndex ->
                when (pageIndex) {
                    0 -> {
                        if (allRemotes.isEmpty()) {
                            EmptyState(
                                text = "No Remotes Yet",
                                secondaryText = "Add a remote to start controlling your devices",
                                icon = Icons.Outlined.SettingsInputAntenna
                            )
                        } else {
                            LazyVerticalGrid(
                                state = remotesGridState,
                                columns = GridCells.Adaptive(160.dp),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(allRemotes.size) { i ->
                                    RemoteTile(
                                        name = allRemotes[i].name,
                                        deviceType = "${allRemotes[i].brand} ${allRemotes[i].type}",
                                        icon = allRemotes[i].getIcon(),
                                        onClick = {
                                            startActivity(
                                                Intent(context, RemoteControlActivity::class.java).apply {
                                                    putExtra("remote", allRemotes[i])
                                                }
                                            )
                                        },
                                        modifier = Modifier.height(140.dp)
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        if (allMacros.isEmpty()) {
                            EmptyState(
                                text = "No Macros Yet",
                                secondaryText = "Create sequences of commands for one-tap control",
                                icon = Icons.Outlined.AutoFixHigh
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = macrosListState,
                                contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
                            ) {
                                items(allMacros.size) { i ->
                                    MacroSequenceCard(
                                        name = allMacros[i].name,
                                        stepCount = allMacros[i].macroUnits.size,
                                        onExecute = {
                                            lifecycleScope.launch(Dispatchers.Main) {
                                                allMacros[i].execute(viewModel.irController, vibrator)
                                            }
                                        },
                                        onEdit = {
                                            startActivity(
                                                Intent(context, AddEditMacroActivity::class.java).apply {
                                                    putExtra("macro_data", allMacros[i])
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}