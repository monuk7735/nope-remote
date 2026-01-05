package com.monuk7735.nope.remote.composables

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.monuk7735.nope.remote.R
import com.monuk7735.nope.remote.models.CustomRemoteLayout
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Composable
fun RemoteControl(
        remoteDataModel: RemoteDataDBModel?,
        onEditRemoteSettings: () -> Unit,
        onBack: () -> Unit
) {
    var layoutLimits by remember { mutableStateOf(Rect(Offset.Zero, 0f)) }

    val shutterButton = remember(remoteDataModel) { remoteDataModel?.getByName("SHUTTER") }
    
    val layoutType = remember(remoteDataModel) {
        remoteDataModel?.getCustomLayoutType() ?: CustomRemoteLayout.NONE
    }

    val isCustomUiAvailable = layoutType != CustomRemoteLayout.NONE
    val context = LocalContext.current
    val sharedPrefs = remember(context) {
        context.getSharedPreferences(context.getString(R.string.shared_pref_app_settings), android.content.Context.MODE_PRIVATE)
    }
    
    val globalPreferCustomUi = remember(sharedPrefs) {
        sharedPrefs.getBoolean("pref_prefer_custom_ui_global", true)
    }

    // Custom UI is shown if:
    // 1. Available for this type
    // 2. Global preference is ON (default true)
    // 3. Remote specific preference is ON (default true)
    // Note: We treat 'null' in DB as true (default)
    var showCustomUi by remember(isCustomUiAvailable, remoteDataModel, globalPreferCustomUi) { 
        mutableStateOf(isCustomUiAvailable && globalPreferCustomUi && (remoteDataModel?.preferCustomUi != false)) 
    }

    Scaffold(
            topBar = {
                AppBar(
                        title = "${remoteDataModel?.name}",
                        onBack = onBack,
                        actions = {
                            if (isCustomUiAvailable) {
                                ActionButton(
                                        name = "Switch Layout",
                                        icon = if (showCustomUi) Icons.Outlined.GridOn else 
                                            if (layoutType == CustomRemoteLayout.CAMERA) Icons.Outlined.CameraAlt else Icons.Outlined.Lightbulb,
                                        onClick = { showCustomUi = !showCustomUi }
                                )
                            }
                            ActionButton(
                                    name = "Settings",
                                    icon = Icons.Outlined.Settings,
                                    onClick = { onEditRemoteSettings() }
                            )
                        }
                )
            },
            content = {
                Box(
                        modifier =
                                Modifier.padding(it)
                                        .fillMaxSize()
                                        .onGloballyPositioned { coordinates ->
                                            layoutLimits = coordinates.boundsInWindow()
                                        }
                                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (showCustomUi && layoutType == CustomRemoteLayout.CAMERA && shutterButton != null) {
                        CameraRemote(
                            remoteDataDBModel = remoteDataModel,
                            shutterButton = shutterButton
                        )
                    } else if (showCustomUi && layoutType == CustomRemoteLayout.LIGHT) {
                        LightRemote(
                            remoteDataDBModel = remoteDataModel
                        )
                    } else {
                        UniversalRemote(
                            remoteDataDBModel = remoteDataModel,
                            layoutLimits = layoutLimits
                        )
                    }
                }
            }
    )
}

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun RemoteControlEditLayout(
        remoteDataModel: RemoteDataDBModel?,
        onSaveLayout: (remoteDataDBModel: RemoteDataDBModel) -> Unit,
        onBack: () -> Unit
) {
    var localRemoteDataDBModel by remember { mutableStateOf(remoteDataModel?.copy()) }
    var layoutLimits by remember { mutableStateOf(Rect(Offset.Zero, 0f)) }
    var gridEnabled by remember { mutableStateOf(false) }
    var showGridSettings by remember { mutableStateOf(false) }
    var gridHCount by remember { mutableIntStateOf(5) }
    var gridVCount by remember { mutableIntStateOf(10) }

    if (showGridSettings) {
        Dialog(onDismissRequest = { showGridSettings = false }) {
            Card(
                    shape = RoundedCornerShape(28.dp),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
            ) {
                Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                            text = "Grid Settings",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enable Grid", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = gridEnabled, onCheckedChange = { gridEnabled = it })
                    }

                    if (gridEnabled) {
                        HorizontalDivider()

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                    "Horizontal Cells: $gridHCount",
                                    style = MaterialTheme.typography.labelLarge
                            )
                            Slider(
                                    value = gridHCount.toFloat(),
                                    onValueChange = { gridHCount = it.toInt() },
                                    valueRange = 2f..12f,
                                    steps = 9
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                    "Vertical Cells: $gridVCount",
                                    style = MaterialTheme.typography.labelLarge
                            )
                            Slider(
                                    value = gridVCount.toFloat(),
                                    onValueChange = { gridVCount = it.toInt() },
                                    valueRange = 2f..20f,
                                    steps = 17
                            )
                        }
                    }

                    TextButton(
                            onClick = { showGridSettings = false },
                            modifier = Modifier.align(Alignment.End)
                    ) { Text("Done") }
                }
            }
        }
    }

    Scaffold(
            topBar = {
                AppBar(
                        title = "Edit Layout",
                        onBack = onBack,
                        actions = {
                            IconButton(onClick = { showGridSettings = true }) {
                                Icon(
                                        imageVector =
                                                if (gridEnabled) Icons.Outlined.GridOn
                                                else Icons.Outlined.GridOff,
                                        contentDescription = "Grid Settings"
                                )
                            }

                            IconButton(
                                    onClick = { localRemoteDataDBModel?.let { onSaveLayout(it) } }
                            ) { Icon(Icons.Outlined.Save, contentDescription = "Save") }
                        }
                )
            },
            content = { innerPadding ->
                Box(
                        modifier =
                                Modifier.padding(innerPadding).fillMaxSize().onGloballyPositioned {
                                        coordinates ->
                                    layoutLimits = coordinates.boundsInWindow()
                                }
                ) {
                    EditableRemote(
                            remoteDataDBModel = localRemoteDataDBModel,
                            onUpdate = { dbModel -> localRemoteDataDBModel = dbModel },
                            layoutLimits = layoutLimits,
                            gridEnabled = gridEnabled,
                            hCount = gridHCount,
                            vCount = gridVCount
                    )
                }
            }
    )
}

@ExperimentalMaterial3Api
@Composable
fun RemoteControlSettings(
        remoteDataModel: RemoteDataDBModel?,
        onEditLayout: () -> Unit,
        onSave: (remoteDataModel: RemoteDataDBModel) -> Unit,
        onDelete: (remoteDataModel: RemoteDataDBModel) -> Unit,
        onBack: () -> Unit
) {
    var remoteName by remember { mutableStateOf(remoteDataModel?.name ?: "") }
    var deleteDialogVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Scaffold(
            topBar = {
                AppBar(
                        title = "Settings",
                        onBack = onBack,
                        actions = {
                            IconButton(
                                    onClick = {
                                        if (remoteName.isBlank()) {
                                            Toast.makeText(
                                                            context,
                                                            "Name cannot be empty",
                                                            Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                        } else {
                                            remoteDataModel?.let {
                                                onSave(it.copy(name = remoteName))
                                            }
                                        }
                                    }
                            ) { Icon(Icons.Outlined.Save, contentDescription = "Save") }
                        }
                )
            }
    ) { paddingValues ->
        Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Remote Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = remoteName,
                        onValueChange = { remoteName = it },
                        label = { Text("Remote Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Beta: Default UI Preference
                    if (remoteDataModel?.getCustomLayoutType() != CustomRemoteLayout.NONE) {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        val sharedPrefs = remember(context) {
                            context.getSharedPreferences(context.getString(R.string.shared_pref_app_settings), android.content.Context.MODE_PRIVATE)
                        }
                        val globalPreferCustomUi = remember(sharedPrefs) {
                            sharedPrefs.getBoolean("pref_prefer_custom_ui_global", true)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Use Custom UI by Default",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (globalPreferCustomUi) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                                Text(
                                    text = if (globalPreferCustomUi) "Beta feature" else "Disabled by global setting",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (globalPreferCustomUi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                            Switch(
                                checked = remoteDataModel?.preferCustomUi != false,
                                enabled = globalPreferCustomUi,
                                onCheckedChange = { checked ->
                                    remoteDataModel?.let {
                                        onSave(it.copy(preferCustomUi = checked))
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Card(
                    onClick = onEditLayout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
            ) {
                Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                                text = "Customize Layout",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                        )
                        Text(
                                text = "Rearrange or hide buttons",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                    onClick = { deleteDialogVisible = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                    shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Remote", fontWeight = FontWeight.SemiBold)
            }
        }

        if (deleteDialogVisible) {
            AlertDialog(
                    onDismissRequest = { deleteDialogVisible = false },
                    title = { Text("Delete Remote?") },
                    text = {
                        Text(
                                "This will permanently remove this remote from your list. This action cannot be undone."
                        )
                    },
                    confirmButton = {
                        TextButton(
                                onClick = {
                                    remoteDataModel?.let { onDelete(it) }
                                    deleteDialogVisible = false
                                },
                                colors =
                                        ButtonDefaults.textButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                        )
                        ) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { deleteDialogVisible = false }) { Text("Cancel") }
                    },
                    shape = RoundedCornerShape(28.dp)
            )
        }
    }
}
