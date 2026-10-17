package com.monuk7735.nope.remote

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsInputAntenna
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.monuk7735.nope.remote.androidtv.discovery.AndroidTvDiscoveryManager
import com.monuk7735.nope.remote.androidtv.discovery.DiscoveredTvDevice
import com.monuk7735.nope.remote.androidtv.ssl.AndroidTvSslManager
import com.monuk7735.nope.remote.composables.AppBar
import com.monuk7735.nope.remote.composables.EmptyState
import com.monuk7735.nope.remote.composables.MacroSequenceCard
import com.monuk7735.nope.remote.composables.RemoteTile
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.ui.theme.NopeRemoteTheme
import com.monuk7735.nope.remote.ui.theme.rememberThemeSettings
import com.monuk7735.nope.remote.viewmodels.HomeActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            val themeSettings = rememberThemeSettings()
            NopeRemoteTheme(
                useDarkTheme = themeSettings.useDarkTheme,
                useDynamicColors = themeSettings.useDynamicColors,
            ) { Root() }
        }
    }

    @Composable
    fun Root() {
        val pageCount = if (viewModel.hasIrBlaster) 2 else 1
        val pagerState = rememberPagerState(pageCount = { pageCount })
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

        val discoveryManager = remember { AndroidTvDiscoveryManager(context) }
        val discoveredTvs by discoveryManager.discoveredDevices.collectAsState()
        val isDiscovering by discoveryManager.isDiscovering.collectAsState()

        DisposableEffect(Unit) {
            discoveryManager.startDiscovery()
            onDispose {
                discoveryManager.stopDiscovery()
            }
        }

        val lifecycleOwner = LocalLifecycleOwner.current
        var openingTvHost by remember { mutableStateOf<String?>(null) }

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    openingTvHost = null
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                try {
                    AndroidTvSslManager.getInstance(context)
                } catch (_: Exception) {}
            }
        }

        var showManualIpDialog by remember { mutableStateOf(false) }
        var selectedRemoteForDeletion by remember { mutableStateOf<RemoteDataDBModel?>(null) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                AppBar(title = stringResource(id = R.string.app_name)) {
                    Surface(
                        onClick = {
                            startActivity(Intent(context, SettingsActivity::class.java))
                        },
                        shape = CircleShape,
                        color = Color.Transparent,
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            },
            bottomBar = {
                if (viewModel.hasIrBlaster) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                    ) {
                        NavigationBarItem(
                            selected = pagerState.currentPage == 0,
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(0) }
                            },
                            label = {
                                Text(
                                    "Remotes",
                                    fontWeight = if (pagerState.currentPage == 0) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    },
                                )
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_ir_remote),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    .copy(alpha = 0.5f),
                            ),
                        )
                        NavigationBarItem(
                            selected = pagerState.currentPage == 1,
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            },
                            label = {
                                Text(
                                    "Macros",
                                    fontWeight = if (pagerState.currentPage == 1) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    },
                                )
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_macro),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    .copy(alpha = 0.5f),
                            ),
                        )
                    }
                }
            },
            floatingActionButton = {
                val fabColor = MaterialTheme.colorScheme.primary

                Surface(
                    onClick = {
                        if (pagerState.currentPage == 0) {
                            if (viewModel.hasIrBlaster) {
                                startActivity(Intent(context, AddRemoteActivity::class.java))
                            } else {
                                showManualIpDialog = true
                            }
                        } else {
                            startActivity(Intent(context, AddEditMacroActivity::class.java))
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    color = fabColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = if (isFabExpanded) 24.dp else 16.dp,
                            vertical = 16.dp,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )

                        AnimatedVisibility(
                            visible = isFabExpanded,
                            enter = expandHorizontally() + fadeIn(),
                            exit = shrinkHorizontally() + fadeOut(),
                        ) {
                            Row {
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (pagerState.currentPage == 0) {
                                        if (viewModel.hasIrBlaster) {
                                            "Add Remote"
                                        } else {
                                            "Connect TV"
                                        }
                                    } else {
                                        "Add Macro"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            },
        ) { innerPadding ->
            val vibrator = remember {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager =
                        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as
                            VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Vibrator::class.java)!!
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
            ) { pageIndex ->
                when (pageIndex) {
                    0 -> {
                        LazyVerticalGrid(
                            state = remotesGridState,
                            columns = GridCells.Adaptive(160.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 88.dp,
                            ),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            if (!viewModel.hasIrBlaster) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme
                                                .colorScheme.surfaceContainerLow,
                                        ),
                                        shape = RoundedCornerShape(24.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme
                                                    .colorScheme.primaryContainer,
                                                modifier = Modifier.size(40.dp),
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Wifi,
                                                        contentDescription = null,
                                                        tint = MaterialTheme
                                                            .colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(20.dp),
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = "Wi-Fi Remote Mode",
                                                    style = MaterialTheme
                                                        .typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme
                                                        .colorScheme.onSurface,
                                                )
                                                Text(
                                                    text = "No IR blaster detected. " +
                                                        "Controlling Android TV over Wi-Fi.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme
                                                        .colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item(span = { GridItemSpan(maxLineSpan) }) {
                                DiscoveredTvSection(
                                    discoveredTvs = discoveredTvs,
                                    isDiscovering = isDiscovering,
                                    onRescan = { discoveryManager.rescan() },
                                    openingTvHost = openingTvHost,
                                    onTvClick = { tv ->
                                        openingTvHost = tv.host
                                        startActivity(
                                            Intent(
                                                context,
                                                AndroidTvRemoteActivity::class.java,
                                            ).apply {
                                                putExtra("host", tv.host)
                                                putExtra("name", tv.name)
                                            },
                                        )
                                    },
                                )
                            }

                            if (allRemotes.isNotEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, bottom = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            painter = painterResource(
                                                id = R.drawable.ic_ir_remote,
                                            ),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "IR Remotes",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }

                                items(allRemotes.size) { i ->
                                    RemoteTile(
                                        name = allRemotes[i].name,
                                        deviceType =
                                        "${allRemotes[i].brand} ${allRemotes[i].type}",
                                        icon = allRemotes[i].getIcon(),
                                        onClick = {
                                            if (!viewModel.hasIrBlaster) {
                                                Toast.makeText(
                                                    context,
                                                    "This remote requires an IR blaster, " +
                                                        "which was not found on this device.",
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                            } else {
                                                startActivity(
                                                    Intent(
                                                        context,
                                                        RemoteControlActivity::class.java,
                                                    ).apply {
                                                        putExtra("remote", allRemotes[i])
                                                    },
                                                )
                                            }
                                        },
                                        onLongClick = {
                                            selectedRemoteForDeletion = allRemotes[i]
                                        },
                                        modifier = Modifier.height(140.dp),
                                    )
                                }
                            } else if (viewModel.hasIrBlaster) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme
                                                .colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                imageVector =
                                                Icons.Outlined.SettingsInputAntenna,
                                                contentDescription = null,
                                                tint = MaterialTheme
                                                    .colorScheme.onSurfaceVariant,
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "No IR remotes added yet. " +
                                                    "Tap '+' to add one.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme
                                                    .colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        if (allMacros.isEmpty()) {
                            EmptyState(
                                text = "No Macros Yet",
                                secondaryText =
                                "Create sequences of commands for one-tap control",
                                icon = Icons.Outlined.AutoFixHigh,
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = macrosListState,
                                contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
                            ) {
                                items(allMacros.size) { i ->
                                    MacroSequenceCard(
                                        name = allMacros[i].name,
                                        stepCount = allMacros[i].macroUnits.size,
                                        onExecute = {
                                            if (!viewModel.hasIrBlaster) {
                                                Toast.makeText(
                                                    context,
                                                    "IR macros require an IR blaster.",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            } else {
                                                lifecycleScope.launch(Dispatchers.Main) {
                                                    allMacros[i].execute(
                                                        viewModel.irController,
                                                        vibrator,
                                                    )
                                                }
                                            }
                                        },
                                        onEdit = {
                                            startActivity(
                                                Intent(
                                                    context,
                                                    AddEditMacroActivity::class.java,
                                                ).apply {
                                                    putExtra(
                                                        "macro_data",
                                                        allMacros[i],
                                                    )
                                                },
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showManualIpDialog) {
                ManualIpConnectDialog(
                    onDismiss = { showManualIpDialog = false },
                    onConnect = { host, name ->
                        showManualIpDialog = false
                        startActivity(
                            Intent(context, AndroidTvRemoteActivity::class.java).apply {
                                putExtra("host", host)
                                putExtra("name", name)
                            },
                        )
                    },
                )
            }

            if (selectedRemoteForDeletion != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectedRemoteForDeletion = null },
                    containerColor = MaterialTheme.colorScheme.surface,
                    sheetState = rememberModalBottomSheetState(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Delete Remote?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Are you sure you want to delete " +
                                "\"${selectedRemoteForDeletion?.name}\"? " +
                                "This action cannot be undone.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                selectedRemoteForDeletion?.let { remote ->
                                    viewModel.deleteRemote(remote)
                                }
                                selectedRemoteForDeletion = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Delete") }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { selectedRemoteForDeletion = null },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Cancel") }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveredTvSection(
    discoveredTvs: List<DiscoveredTvDevice>,
    isDiscovering: Boolean,
    onRescan: () -> Unit,
    openingTvHost: String? = null,
    onTvClick: (DiscoveredTvDevice) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Tv,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Android TVs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (isDiscovering) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                    )
                }
            }
            IconButton(
                onClick = onRescan,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Rescan",
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        if (discoveredTvs.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(discoveredTvs.size) { i ->
                    DiscoveredTvCard(
                        device = discoveredTvs[i],
                        isOpening = discoveredTvs[i].host == openingTvHost,
                        onClick = { onTvClick(discoveredTvs[i]) },
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (isDiscovering) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Searching for Android TVs...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        Text(
                            text = "No Android TVs Found",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Make sure your TV is powered on and connected to " +
                                "the same Wi-Fi network.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveredTvCard(
    device: DiscoveredTvDevice,
    isOpening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .width(200.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = !isOpening, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Tv,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                if (isOpening) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Text(
                            text = "Wi-Fi",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Column {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = device.host,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ManualIpConnectDialog(
    onDismiss: () -> Unit,
    onConnect: (host: String, name: String) -> Unit,
) {
    var ipInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Connect to Android TV") },
        text = {
            Column {
                Text(
                    text = "Enter the IP address of your Android TV or " +
                        "Google TV on your local network.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = ipInput,
                    onValueChange = { input ->
                        ipInput = input.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("TV IP Address") },
                    placeholder = { Text("e.g. 192.168.1.100") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("TV Name (Optional)") },
                    placeholder = { Text("Android TV") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val host = ipInput.trim()
                    val name = nameInput.trim().ifEmpty { "Android TV" }
                    onConnect(host, name)
                },
                enabled = ipInput.trim().isNotEmpty(),
            ) {
                Text("Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
