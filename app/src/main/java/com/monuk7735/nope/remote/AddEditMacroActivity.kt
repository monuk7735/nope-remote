package com.monuk7735.nope.remote

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.lifecycle.ViewModelProvider
import com.monuk7735.nope.remote.composables.AddMacroUnitDialog
import com.monuk7735.nope.remote.composables.AppBar
import com.monuk7735.nope.remote.composables.MacroStepItem
import com.monuk7735.nope.remote.composables.utils.detectReorder
import com.monuk7735.nope.remote.composables.utils.draggedItem
import com.monuk7735.nope.remote.composables.utils.rememberReorderableState
import com.monuk7735.nope.remote.models.database.MacroDataDBModel
import com.monuk7735.nope.remote.ui.theme.NopeRemoteTheme
import com.monuk7735.nope.remote.ui.theme.rememberThemeSettings
import com.monuk7735.nope.remote.viewmodels.AddEditMacroViewModel
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
class AddEditMacroActivity : ComponentActivity() {

    private lateinit var viewModel: AddEditMacroViewModel
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[AddEditMacroViewModel::class.java]

        val initialMacro =
            IntentCompat.getParcelableExtra(intent, "macro_data", MacroDataDBModel::class.java)
                ?: MacroDataDBModel(0, "", listOf())

        isEditing = initialMacro.id != 0
        viewModel.initialize(initialMacro)

        setContent {
            val themeSettings = rememberThemeSettings()
            NopeRemoteTheme(
                useDarkTheme = themeSettings.useDarkTheme,
                useDynamicColors = themeSettings.useDynamicColors,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AddEditMacroRoot(initialMacro.id)
                }
            }
        }
    }

    @Composable
    fun AddEditMacroRoot(macroId: Int) {
        val allRemotes by viewModel.allRemotes.observeAsState(emptyList())
        val macroUnits = viewModel.macroUnits
        val macroName = viewModel.macroName

        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        val reorderableState = rememberReorderableState(
            listState = rememberLazyListState(),
            onMove = { from, to -> viewModel.reorderUnits(from, to) },
        )

        var editingIndex by remember { mutableIntStateOf(-1) }
        var showAddDialog by remember { mutableStateOf(false) }
        var showDeleteConfirm by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                AppBar(
                    title = if (isEditing) "Edit Macro" else "New Macro",
                    onBack = { finish() },
                ) {
                    IconButton(onClick = {
                        scope.launch {
                            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val vibratorManager = context.getSystemService(
                                    Context.VIBRATOR_MANAGER_SERVICE,
                                ) as VibratorManager
                                vibratorManager.defaultVibrator
                            } else {
                                context.getSystemService(Vibrator::class.java)!!
                            }
                            MacroDataDBModel(0, macroName, macroUnits.toList())
                                .execute(viewModel.irController, vibrator)
                        }
                    }) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = "Test Macro")
                    }

                    if (isEditing) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Delete Macro",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            },
            bottomBar = {
                Surface(
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .windowInsetsPadding(WindowInsets.navigationBars),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                editingIndex = -1
                                showAddDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Command")
                        }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (macroName.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "Please enter a name",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                    return@Button
                                }
                                if (macroUnits.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "Add at least one command",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                    return@Button
                                }

                                val macro = MacroDataDBModel(
                                    id = macroId,
                                    name = macroName,
                                    macroUnits = macroUnits.toList(),
                                )

                                if (isEditing) {
                                    viewModel.updateMacro(macro)
                                } else {
                                    viewModel.addMacro(macro)
                                }

                                finish()
                            },
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Outlined.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save Macro")
                        }
                    }
                }
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    TextField(
                        value = macroName,
                        onValueChange = { viewModel.macroName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Enter Macro Name",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.6f,
                                ),
                            )
                        },
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        singleLine = true,
                    )
                }

                if (macroUnits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.SettingsBackupRestore,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outlineVariant,
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No commands yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                "Add commands to build your sequence",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        state = reorderableState.listState,
                        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
                    ) {
                        items(macroUnits.size, key = { it }) { i ->
                            val unit = macroUnits[i]
                            val remote = allRemotes.find { it.id == unit.sourceRemoteId }

                            Box(modifier = Modifier.draggedItem(reorderableState, i)) {
                                MacroStepItem(
                                    stepNumber = i + 1,
                                    macroTransmit = unit,
                                    remoteData = remote,
                                    isLast = i == macroUnits.size - 1,
                                    onDelete = { viewModel.removeUnit(i) },
                                    onClick = {
                                        editingIndex = i
                                        showAddDialog = true
                                    },
                                    dragModifier = Modifier.detectReorder(reorderableState, i),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddMacroUnitDialog(
                viewModel = viewModel,
                macroTransmit = if (editingIndex >= 0) macroUnits[editingIndex] else null,
                index = editingIndex,
                onAdd = {
                    viewModel.addUnit(it)
                    showAddDialog = false
                },
                onUpdate = { unit, idx ->
                    viewModel.updateUnit(idx, unit)
                    showAddDialog = false
                },
                onDismiss = { showAddDialog = false },
                onDelete = {
                    viewModel.removeUnit(editingIndex)
                    showAddDialog = false
                },
            )
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete Macro?") },
                text = {
                    Text(
                        "Are you sure you want to delete this macro? This action cannot be undone.",
                    )
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                        onClick = {
                            viewModel.deleteMacro(
                                MacroDataDBModel(macroId, macroName, macroUnits.toList()),
                            )
                            finish()
                        },
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                },
            )
        }
    }
}
