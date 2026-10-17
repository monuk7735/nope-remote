package com.monuk7735.nope.remote.composables

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.VolumeDown
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.monuk7735.nope.remote.R
import com.monuk7735.nope.remote.androidtv.pairing.AndroidTvPairingManager
import com.monuk7735.nope.remote.androidtv.proto.remote.Remotemessage
import com.monuk7735.nope.remote.androidtv.protocol.AndroidTvClient
import com.monuk7735.nope.remote.androidtv.ssl.AndroidTvSslManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class PairingStatus {
    IDLE,
    PAIRING,
    CANCELLED,
    FAILED,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidTvRemoteScreen(
    host: String,
    deviceName: String,
    onBack: () -> Unit,
    onRegisterVolumeHandler: (((KeyEvent) -> Boolean)?) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val prefs = remember {
        context.getSharedPreferences(
            context.getString(R.string.shared_pref_app_settings),
            Context.MODE_PRIVATE,
        )
    }

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(
                Context.VIBRATOR_MANAGER_SERVICE,
            ) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    val sslManager = remember { AndroidTvSslManager(context) }
    val client = remember { AndroidTvClient(sslManager) }
    val pairingManager = remember { AndroidTvPairingManager(sslManager) }

    fun vibrate() {
        val shouldVibrate = prefs.getBoolean(
            context.getString(R.string.pref_settings_vibration),
            true,
        )
        if (!shouldVibrate) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    android.os.VibrationEffect.createOneShot(
                        35,
                        android.os.VibrationEffect.DEFAULT_AMPLITUDE,
                    ),
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    fun sendKey(key: Remotemessage.RemoteKeyCode) {
        vibrate()
        client.sendKey(key)
    }

    val connectionState by client.connectionState.collectAsState()

    DisposableEffect(connectionState) {
        onRegisterVolumeHandler { event ->
            val volumeKeysEnabled = prefs.getBoolean("pref_android_tv_volume_keys", true)
            if (!volumeKeysEnabled ||
                connectionState !is AndroidTvClient.ConnectionState.Connected
            ) {
                return@onRegisterVolumeHandler false
            }

            if (event.action == KeyEvent.ACTION_DOWN) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_VOLUME_UP -> {
                        sendKey(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_UP)
                    }
                    KeyEvent.KEYCODE_VOLUME_DOWN -> {
                        sendKey(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_DOWN)
                    }
                }
            }
            true
        }
        onDispose {
            onRegisterVolumeHandler(null)
        }
    }

    var isPaired by remember { mutableStateOf(false) }
    var pairingStatus by remember { mutableStateOf(PairingStatus.IDLE) }
    var pairingErrorMessage by remember { mutableStateOf<String?>(null) }

    var showKeyboardDialog by remember { mutableStateOf(false) }
    var textToSend by remember { mutableStateOf("") }

    // Pairing dialog state
    var showPairingDialog by remember { mutableStateOf(false) }
    var isPairingLoading by remember { mutableStateOf(false) }
    var pairingPin by remember { mutableStateOf("") }
    var pairingError by remember { mutableStateOf<String?>(null) }
    var activePairingSession by remember {
        mutableStateOf<AndroidTvPairingManager.ActivePairingSession?>(null)
    }

    DisposableEffect(host) {
        client.connect(host)
        onDispose {
            client.disconnect()
            activePairingSession?.close()
        }
    }

    fun startPairing() {
        client.disconnect()
        pairingStatus = PairingStatus.PAIRING
        isPairingLoading = true
        pairingError = null
        showPairingDialog = true
        pairingPin = ""
        scope.launch {
            try {
                val session = pairingManager.startPairing(host)
                activePairingSession = session
                isPairingLoading = false
            } catch (e: Exception) {
                isPairingLoading = false
                pairingStatus = PairingStatus.FAILED
                val (_, desc) = AndroidTvClient.formatConnectionError(e, host)
                pairingErrorMessage = desc
                showPairingDialog = false
            }
        }
    }

    fun submitPin(pin: String) {
        val session = activePairingSession ?: return
        isPairingLoading = true
        pairingError = null
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    session.submitPin(pin)
                    true
                } catch (e: Exception) {
                    pairingError = e.message ?: "Incorrect pairing code"
                    false
                }
            }
            isPairingLoading = false
            if (result) {
                session.close()
                activePairingSession = null
                showPairingDialog = false
                pairingStatus = PairingStatus.IDLE
                isPaired = true
                Toast.makeText(context, "Pairing successful", Toast.LENGTH_SHORT).show()
                client.connect(host)
            } else {
                pairingStatus = PairingStatus.FAILED
                pairingErrorMessage = pairingError ?: "Pairing failed"
            }
        }
    }

    LaunchedEffect(connectionState) {
        when (connectionState) {
            is AndroidTvClient.ConnectionState.Connected -> {
                isPaired = true
                pairingStatus = PairingStatus.IDLE
                showPairingDialog = false
            }
            is AndroidTvClient.ConnectionState.NeedsPairing -> {
                isPaired = false
                if (pairingStatus != PairingStatus.CANCELLED &&
                    pairingStatus != PairingStatus.FAILED
                ) {
                    startPairing()
                }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = deviceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (isPaired) {
                        when (connectionState) {
                            is AndroidTvClient.ConnectionState.Connected -> {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                                    contentColor = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(end = 12.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 6.dp,
                                        ),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF4CAF50)),
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Connected",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                            is AndroidTvClient.ConnectionState.Connecting -> {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(
                                        alpha = 0.5f,
                                    ),
                                    modifier = Modifier.padding(end = 12.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 6.dp,
                                        ),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Connecting...",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    }
                                }
                            }
                            else -> {
                                OutlinedButton(
                                    onClick = { client.connect(host) },
                                    contentPadding = PaddingValues(
                                        horizontal = 12.dp,
                                        vertical = 4.dp,
                                    ),
                                    shape = CircleShape,
                                    modifier = Modifier.padding(end = 12.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Reconnect",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        if (isPaired) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Top Row: Power & Keyboard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    AndroidTvButton(
                        icon = Icons.Outlined.PowerSettingsNew,
                        contentDescription = "Power",
                        tint = MaterialTheme.colorScheme.error,
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(
                            alpha = 0.4f,
                        ),
                        onClick = { sendKey(Remotemessage.RemoteKeyCode.KEYCODE_POWER) },
                    )

                    AndroidTvButton(
                        icon = Icons.Outlined.Keyboard,
                        contentDescription = "Text Input",
                        tint = MaterialTheme.colorScheme.primary,
                        onClick = { showKeyboardDialog = true },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // D-Pad Navigation Cluster
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    // Up
                    IconButton(
                        onClick = { sendKey(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_UP) },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .size(56.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowUp,
                            contentDescription = "Up",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    // Down
                    IconButton(
                        onClick = { sendKey(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_DOWN) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                            .size(56.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Down",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    // Left
                    IconButton(
                        onClick = { sendKey(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_LEFT) },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 10.dp)
                            .size(56.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                            contentDescription = "Left",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    // Right
                    IconButton(
                        onClick = { sendKey(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_RIGHT) },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 10.dp)
                            .size(56.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = "Right",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    // Center OK
                    Surface(
                        onClick = { sendKey(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_CENTER) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(76.dp),
                        tonalElevation = 4.dp,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "OK",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // System Buttons: Back, Home, Menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    AndroidTvButton(
                        icon = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        onClick = { sendKey(Remotemessage.RemoteKeyCode.KEYCODE_BACK) },
                    )

                    AndroidTvButton(
                        icon = Icons.Outlined.Home,
                        contentDescription = "Home",
                        onClick = { sendKey(Remotemessage.RemoteKeyCode.KEYCODE_HOME) },
                    )

                    AndroidTvButton(
                        icon = Icons.Outlined.Menu,
                        contentDescription = "Menu",
                        onClick = { sendKey(Remotemessage.RemoteKeyCode.KEYCODE_MENU) },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Volume Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    AndroidTvButton(
                        icon = Icons.AutoMirrored.Outlined.VolumeUp,
                        contentDescription = "Volume Up",
                        onClick = { sendKey(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_UP) },
                    )

                    AndroidTvButton(
                        icon = Icons.AutoMirrored.Outlined.VolumeOff,
                        contentDescription = "Mute",
                        onClick = { sendKey(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_MUTE) },
                    )

                    AndroidTvButton(
                        icon = Icons.AutoMirrored.Outlined.VolumeDown,
                        contentDescription = "Volume Down",
                        onClick = { sendKey(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_DOWN) },
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    when (pairingStatus) {
                        PairingStatus.PAIRING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 4.dp,
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Pairing",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Follow the prompt on your TV screen to pair",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                        PairingStatus.CANCELLED -> {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(72.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Tv,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Pairing Cancelled",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Pairing is required to control this TV.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                OutlinedButton(
                                    onClick = onBack,
                                    shape = RoundedCornerShape(24.dp),
                                ) {
                                    Text("Go Back")
                                }
                                Button(
                                    onClick = { startPairing() },
                                    shape = RoundedCornerShape(24.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pair TV")
                                }
                            }
                        }
                        PairingStatus.FAILED -> {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.size(72.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(36.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            val isNotFound = pairingErrorMessage
                                ?.contains("find or reach", ignoreCase = true) == true
                            Text(
                                text = if (isNotFound) "Device Not Found" else "Pairing Failed",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = pairingErrorMessage
                                    ?: "Could not pair with the TV. Please try again.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                OutlinedButton(
                                    onClick = onBack,
                                    shape = RoundedCornerShape(24.dp),
                                ) {
                                    Text("Go Back")
                                }
                                Button(
                                    onClick = { startPairing() },
                                    shape = RoundedCornerShape(24.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Try Again")
                                }
                            }
                        }
                        PairingStatus.IDLE -> {
                            val state = connectionState
                            if (state is AndroidTvClient.ConnectionState.Error) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    modifier = Modifier.size(72.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.ErrorOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.size(36.dp),
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = state.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    OutlinedButton(
                                        onClick = onBack,
                                        shape = RoundedCornerShape(24.dp),
                                    ) {
                                        Text("Go Back")
                                    }
                                    Button(
                                        onClick = { client.connect(host) },
                                        shape = RoundedCornerShape(24.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Retry")
                                    }
                                }
                            } else {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    strokeWidth = 4.dp,
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Connecting...",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Keyboard Text Input Dialog
    if (showKeyboardDialog) {
        AlertDialog(
            onDismissRequest = { showKeyboardDialog = false },
            title = { Text("Keyboard Input") },
            text = {
                Column {
                    Text(
                        text = "Type text to inject directly into the active " +
                            "input field on your TV.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = textToSend,
                        onValueChange = { textToSend = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search or type here...") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (textToSend.isNotEmpty()) {
                            vibrate()
                            client.sendText(textToSend)
                            textToSend = ""
                            showKeyboardDialog = false
                        }
                    },
                ) {
                    Text("Send to TV")
                }
            },
            dismissButton = {
                TextButton(onClick = { showKeyboardDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Pairing Dialog
    if (showPairingDialog) {
        AlertDialog(
            onDismissRequest = {
                activePairingSession?.close()
                activePairingSession = null
                showPairingDialog = false
                pairingStatus = PairingStatus.CANCELLED
            },
            title = { Text("Pairing with TV") },
            text = {
                Column {
                    if (isPairingLoading && activePairingSession == null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Initiating connection with TV...")
                        }
                    } else {
                        Text(
                            text = "Enter the 6-character alphanumeric code " +
                                "displayed on your TV screen:",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = pairingPin,
                            onValueChange = {
                                if (it.length <= 6) pairingPin = it.uppercase()
                            },
                            label = { Text("6-character code") },
                            placeholder = { Text("e.g. 1A2B3C") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            isError = pairingError != null,
                        )
                        if (pairingError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = pairingError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (activePairingSession != null) {
                    Button(
                        onClick = { submitPin(pairingPin) },
                        enabled = pairingPin.length == 6 && !isPairingLoading,
                    ) {
                        if (isPairingLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text("Pair")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        activePairingSession?.close()
                        activePairingSession = null
                        showPairingDialog = false
                        pairingStatus = PairingStatus.CANCELLED
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun AndroidTvButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        modifier = Modifier.size(56.dp),
        tonalElevation = 2.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}
