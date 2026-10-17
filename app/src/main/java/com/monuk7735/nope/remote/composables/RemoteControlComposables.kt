package com.monuk7735.nope.remote.composables

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridOff
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsRemote
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.monuk7735.nope.remote.R
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.models.CustomRemoteLayout
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import kotlin.math.round

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Composable
fun RemoteControl(
    remoteDataModel: RemoteDataDBModel?,
    onEditRemoteSettings: () -> Unit,
    onEditLayout: () -> Unit = {},
    onBack: () -> Unit,
) {
    var layoutLimits by remember { mutableStateOf(Rect(Offset.Zero, 0f)) }

    val shutterButton = remember(remoteDataModel) { remoteDataModel?.getByName("SHUTTER") }

    val layoutType = remember(remoteDataModel) {
        remoteDataModel?.getCustomLayoutType() ?: CustomRemoteLayout.NONE
    }

    val isCustomUiAvailable = layoutType != CustomRemoteLayout.NONE
    val context = LocalContext.current
    val sharedPrefs = remember(context) {
        context.getSharedPreferences(
            context.getString(R.string.shared_pref_app_settings),
            Context.MODE_PRIVATE,
        )
    }

    val globalPreferCustomUi = remember(sharedPrefs) {
        sharedPrefs.getBoolean("pref_prefer_custom_ui_global", true)
    }

    val textOnlyButtons = remember(sharedPrefs) {
        sharedPrefs.getBoolean("pref_text_buttons_only", false)
    }

    val isEmpty = (
        remoteDataModel?.onScreenRemoteButtonDBS.isNullOrEmpty() &&
            remoteDataModel?.offScreenRemoteButtonDBS.isNullOrEmpty()
        )

    // Custom UI is shown if:
    // 1. Available for this type
    // 2. Global preference is ON (default true)
    // 3. Remote specific preference is ON (default true)
    // Note: We treat 'null' in DB as true (default)
    var showCustomUi by remember(isCustomUiAvailable, remoteDataModel, globalPreferCustomUi) {
        val remotePrefers = remoteDataModel?.preferCustomUi != false
        mutableStateOf(isCustomUiAvailable && globalPreferCustomUi && remotePrefers)
    }

    Scaffold(
        topBar = {
            AppBar(
                title = "${remoteDataModel?.name}",
                onBack = onBack,
                actions = {
                    if (isCustomUiAvailable && !isEmpty) {
                        val altIcon = if (layoutType == CustomRemoteLayout.CAMERA) {
                            Icons.Outlined.CameraAlt
                        } else {
                            Icons.Outlined.Lightbulb
                        }
                        ActionButton(
                            name = "Switch Layout",
                            icon = if (showCustomUi) Icons.Outlined.GridOn else altIcon,
                            onClick = { showCustomUi = !showCustomUi },
                        )
                    }
                    ActionButton(
                        name = "Settings",
                        icon = Icons.Outlined.Settings,
                        onClick = { onEditRemoteSettings() },
                    )
                },
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
                    .background(MaterialTheme.colorScheme.background),
            ) {
                if (isEmpty) {
                    EmptyRemoteGuide(
                        remoteDataModel = remoteDataModel,
                        onEditLayout = onEditLayout,
                    )
                } else if (
                    showCustomUi &&
                    layoutType == CustomRemoteLayout.CAMERA &&
                    shutterButton != null
                ) {
                    CameraRemote(
                        remoteDataDBModel = remoteDataModel,
                        shutterButton = shutterButton,
                    )
                } else if (showCustomUi && layoutType == CustomRemoteLayout.LIGHT) {
                    LightRemote(
                        remoteDataDBModel = remoteDataModel,
                    )
                } else {
                    UniversalRemote(
                        remoteDataDBModel = remoteDataModel,
                        layoutLimits = layoutLimits,
                        textOnlyMode = textOnlyButtons,
                    )
                }
            }
        },
    )
}

@Composable
fun EmptyRemoteGuide(
    remoteDataModel: RemoteDataDBModel?,
    onEditLayout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.7f,
            ),
            modifier = Modifier.size(76.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.SettingsRemote,
                    contentDescription = null,
                    modifier = Modifier.size(38.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This Remote is Empty",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Add your custom buttons and IR signals to start " +
                "controlling devices with this remote.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "How to add buttons:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                StepGuideRow(
                    step = "1",
                    title = "Open Layout Editor",
                    description = "Tap 'Edit Layout & Add Buttons' below " +
                        "(or Settings ⚙ > Customize Layout).",
                )

                StepGuideRow(
                    step = "2",
                    title = "Tap '+ Add Key'",
                    description = "Tap the '+ Add Key' button to configure a new button.",
                )

                StepGuideRow(
                    step = "3",
                    title = "Select Protocol or Frequency",
                    description = "Choose from supported protocols " +
                        "(NEC, Sony, Samsung, etc.) or enter carrier frequency and raw pulses.",
                )

                StepGuideRow(
                    step = "4",
                    title = "Position and Save",
                    description = "Drag the button to any slot on the grid, " +
                        "test it, and tap the Save (💾) icon.",
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onEditLayout,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Outlined.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Edit Layout & Add Buttons",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun StepGuideRow(step: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = step,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun RemoteControlEditLayout(
    remoteDataModel: RemoteDataDBModel?,
    onSaveLayout: (remoteDataDBModel: RemoteDataDBModel) -> Unit,
    onBack: () -> Unit,
) {
    var localRemoteDataDBModel by remember { mutableStateOf(remoteDataModel?.copy()) }
    var layoutLimits by remember { mutableStateOf(Rect(Offset.Zero, 0f)) }
    var gridEnabled by remember { mutableStateOf(true) }
    var showGridSettings by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showAddKeyDialog by remember { mutableStateOf(false) }
    var gridHCount by remember { mutableIntStateOf(5) }
    var gridVCount by remember { mutableIntStateOf(10) }

    val hasChanges = localRemoteDataDBModel != remoteDataModel
    val isCustomRemote = localRemoteDataDBModel?.isCustom == true

    BackHandler(enabled = hasChanges) {
        showDiscardDialog = true
    }

    val context = LocalContext.current
    val density = LocalDensity.current

    val irController = remember(context) {
        val manager = context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        if (manager != null) IRController(manager) else null
    }

    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(
                Context.VIBRATOR_MANAGER_SERVICE,
            ) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(Vibrator::class.java)!!
        }
    }

    if (showAddKeyDialog) {
        AddCustomKeyDialog(
            onDismissRequest = { showAddKeyDialog = false },
            onAddKey = { name, pattern ->
                val currentModel = localRemoteDataDBModel ?: return@AddCustomKeyDialog
                val currentOnScreen = currentModel.onScreenRemoteButtonDBS
                val buttonSizePx = with(density) { 72.dp.toPx() }
                val maxRows = (gridVCount - 1).coerceAtLeast(1)
                val gridSizeXPx = if (layoutLimits.width > 0) {
                    layoutLimits.width / gridHCount.toFloat()
                } else {
                    0f
                }
                val gridSizeYPx = if (layoutLimits.height > 0) {
                    layoutLimits.height / gridVCount.toFloat()
                } else {
                    0f
                }

                val occupiedSlots = if (gridSizeXPx > 0 && gridSizeYPx > 0) {
                    currentOnScreen.map { b ->
                        Pair(
                            round(b.offsetX / gridSizeXPx).toInt().coerceIn(0, gridHCount - 1),
                            round(b.offsetY / gridSizeYPx).toInt().coerceIn(0, maxRows - 1),
                        )
                    }.toSet()
                } else {
                    emptySet()
                }

                var targetSlot: Pair<Int, Int>? = null
                for (r in 0 until maxRows) {
                    for (c in 0 until gridHCount) {
                        if (!occupiedSlots.contains(Pair(c, r))) {
                            targetSlot = Pair(c, r)
                            break
                        }
                    }
                    if (targetSlot != null) break
                }

                val (snappedX, snappedY) = if (
                    targetSlot != null && gridSizeXPx > 0 && gridSizeYPx > 0
                ) {
                    val (c, r) = targetSlot
                    Pair(
                        gridSizeXPx * c + (gridSizeXPx - buttonSizePx) / 2,
                        gridSizeYPx * r + (gridSizeYPx - buttonSizePx) / 2,
                    )
                } else {
                    Pair(gridSizeXPx, gridSizeYPx)
                }

                val newButton = RemoteButtonDBModel(
                    offsetX = snappedX,
                    offsetY = snappedY,
                    name = name,
                    irPattern = pattern,
                )

                localRemoteDataDBModel = currentModel.copy(
                    onScreenRemoteButtonDBS = currentOnScreen + newButton,
                )
            },
            irController = irController,
            vibrator = vibrator,
        )
    }

    if (showGridSettings) {
        Dialog(onDismissRequest = { showGridSettings = false }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Grid Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Enable Grid", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = gridEnabled, onCheckedChange = { gridEnabled = it })
                    }

                    if (gridEnabled) {
                        HorizontalDivider()

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Horizontal Cells: $gridHCount",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Slider(
                                value = gridHCount.toFloat(),
                                onValueChange = { gridHCount = it.toInt() },
                                valueRange = 2f..12f,
                                steps = 9,
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Vertical Cells: $gridVCount",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Slider(
                                value = gridVCount.toFloat(),
                                onValueChange = { gridVCount = it.toInt() },
                                valueRange = 2f..20f,
                                steps = 17,
                            )
                        }
                    }

                    TextButton(
                        onClick = { showGridSettings = false },
                        modifier = Modifier.align(Alignment.End),
                    ) { Text("Done") }
                }
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Changes?") },
            text = { Text("Are you sure? You'll lose all your unsaved layout changes.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Editing")
                }
            },
            shape = RoundedCornerShape(28.dp),
        )
    }

    Scaffold(
        topBar = {
            AppBar(
                title = "Edit Layout",
                onBack = {
                    if (hasChanges) {
                        showDiscardDialog = true
                    } else {
                        onBack()
                    }
                },
                actions = {
                    if (isCustomRemote) {
                        IconButton(onClick = { showAddKeyDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.AddCircleOutline,
                                contentDescription = "Add Key",
                            )
                        }
                    }

                    IconButton(onClick = { showGridSettings = true }) {
                        Icon(
                            imageVector =
                            if (gridEnabled) {
                                Icons.Outlined.GridOn
                            } else {
                                Icons.Outlined.GridOff
                            },
                            contentDescription = "Grid Settings",
                        )
                    }

                    IconButton(
                        onClick = { localRemoteDataDBModel?.let { onSaveLayout(it) } },
                    ) { Icon(Icons.Outlined.Save, contentDescription = "Save") }
                },
            )
        },
        floatingActionButton = {
            if (isCustomRemote) {
                ExtendedFloatingActionButton(
                    onClick = { showAddKeyDialog = true },
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("Add Key") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        content = { innerPadding ->
            Box(
                modifier =
                Modifier.padding(
                    innerPadding,
                ).fillMaxSize().onGloballyPositioned { coordinates ->
                    layoutLimits = coordinates.boundsInWindow()
                },
            ) {
                if (localRemoteDataDBModel?.onScreenRemoteButtonDBS.isNullOrEmpty() &&
                    localRemoteDataDBModel?.offScreenRemoteButtonDBS.isNullOrEmpty()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = 0.5f,
                            ),
                            modifier = Modifier.size(64.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.TouchApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Buttons on Layout",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap '+ Add Key' below to add a protocol or " +
                                "frequency IR button.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                EditableRemote(
                    remoteDataDBModel = localRemoteDataDBModel,
                    onUpdate = { dbModel -> localRemoteDataDBModel = dbModel },
                    layoutLimits = layoutLimits,
                    gridEnabled = gridEnabled,
                    hCount = gridHCount,
                    vCount = gridVCount,
                    textOnlyMode = remember(context) {
                        context.getSharedPreferences(
                            context.getString(R.string.shared_pref_app_settings),
                            Context.MODE_PRIVATE,
                        ).getBoolean("pref_text_buttons_only", false)
                    },
                )
            }
        },
    )
}

@ExperimentalMaterial3Api
@Composable
fun RemoteControlSettings(
    remoteDataModel: RemoteDataDBModel?,
    onEditLayout: () -> Unit,
    onSave: (remoteDataModel: RemoteDataDBModel) -> Unit,
    onDelete: (remoteDataModel: RemoteDataDBModel) -> Unit,
    onBack: () -> Unit,
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
                                    Toast.LENGTH_SHORT,
                                )
                                    .show()
                            } else {
                                remoteDataModel?.let {
                                    onSave(it.copy(name = remoteName))
                                }
                            }
                        },
                    ) { Icon(Icons.Outlined.Save, contentDescription = "Save") }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Remote Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (!remoteDataModel?.model.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Model: ${remoteDataModel.model}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = remoteName,
                        onValueChange = { remoteName = it },
                        label = { Text("Remote Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                    )

                    // Beta: Default UI Preference
                    if (remoteDataModel?.getCustomLayoutType() != CustomRemoteLayout.NONE) {
                        Spacer(modifier = Modifier.height(24.dp))

                        val sharedPrefs = remember(context) {
                            context.getSharedPreferences(
                                context.getString(R.string.shared_pref_app_settings),
                                Context.MODE_PRIVATE,
                            )
                        }
                        val globalPreferCustomUi = remember(sharedPrefs) {
                            sharedPrefs.getBoolean("pref_prefer_custom_ui_global", true)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Use Custom UI by Default",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (globalPreferCustomUi) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    },
                                )
                                Text(
                                    text = if (globalPreferCustomUi) {
                                        "Beta feature"
                                    } else {
                                        "Disabled by global setting"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (globalPreferCustomUi) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    },
                                )
                            }
                            Switch(
                                checked = remoteDataModel?.preferCustomUi != false,
                                enabled = globalPreferCustomUi,
                                onCheckedChange = { checked ->
                                    remoteDataModel?.let {
                                        onSave(it.copy(preferCustomUi = checked))
                                    }
                                },
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
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Customize Layout",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Rearrange or hide buttons",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
                shape = RoundedCornerShape(16.dp),
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
                        "This will permanently remove this remote from your list. " +
                            "This action cannot be undone.",
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
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { deleteDialogVisible = false }) { Text("Cancel") }
                },
                shape = RoundedCornerShape(28.dp),
            )
        }
    }
}
