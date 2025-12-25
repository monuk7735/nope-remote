package com.monuk7735.nope.remote

import android.os.Build
import android.os.Bundle

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.monuk7735.nope.remote.composables.ActionButton
import com.monuk7735.nope.remote.composables.AddMacroUnitDialog
import com.monuk7735.nope.remote.composables.MacroUnitComposable
import com.monuk7735.nope.remote.composables.utils.detectReorder
import com.monuk7735.nope.remote.composables.utils.draggedItem
import com.monuk7735.nope.remote.composables.utils.rememberReorderableState
import com.monuk7735.nope.remote.models.custom.macros.MacroTransmit
import com.monuk7735.nope.remote.models.database.MacroDataDBModel
import com.monuk7735.nope.remote.ui.theme.NopeRemoteTheme
import com.monuk7735.nope.remote.viewmodels.AddEditMacroViewModel

@ExperimentalMaterial3Api
class AddEditMacroActivity : ComponentActivity() {

    private lateinit var viewModel: AddEditMacroViewModel

    private var editingMacro = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[AddEditMacroViewModel::class.java]

        val macroDataDBModel: MacroDataDBModel =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("macro_data", MacroDataDBModel::class.java)
            } else {
                intent.getParcelableExtra("macro_data")
            } ?: MacroDataDBModel(0, "", listOf())

        editingMacro = macroDataDBModel.id != 0

        setContent {
            val themeSettings = com.monuk7735.nope.remote.ui.theme.rememberThemeSettings()
            NopeRemoteTheme(
                useDarkTheme = themeSettings.useDarkTheme,
                useDynamicColors = themeSettings.useDynamicColors
            ) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AddEditMacroRoot(macroDataDBModel)
                }
            }
        }
    }

    @Composable
    fun AddEditMacroRoot(macroDataDBModel: MacroDataDBModel) {

        val allRemotes by viewModel.allRemotes.observeAsState(emptyList())

        val allMacroUnits: MutableList<MacroTransmit> = remember {
            mutableStateListOf()
        }

        LaunchedEffect(Unit) {
            if (allMacroUnits.isEmpty()) {
                allMacroUnits.addAll(macroDataDBModel.macroUnits)
            }
        }

        var index: Int by remember {
            mutableStateOf(0)
        }

        var dialogVisible by remember {
            mutableStateOf(false)
        }
        
        var deleteConfirmationVisible by remember {
            mutableStateOf(false)
        }

        var macroName by remember {
            mutableStateOf(macroDataDBModel.name)
        }

        Scaffold(
            topBar = {
                com.monuk7735.nope.remote.composables.AppBar(
                    title = if (editingMacro) "Edit Macro" else "Create Macro",
                    onBack = {
                        finish()
                    }
                ) {
                    if (editingMacro)
                        ActionButton(
                            name = "Delete",
                            icon = Icons.Outlined.Delete,
                            onClick = {
                                deleteConfirmationVisible = true
                            })
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = {
                        Text(text = "Save")
                    },
                    onClick = {
                         if (macroName.isEmpty()) {
                            Toast
                                .makeText(
                                    applicationContext,
                                    "Empty macro name",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                            return@ExtendedFloatingActionButton
                        }
                        if (allMacroUnits.size == 0) {
                            Toast
                                .makeText(
                                    applicationContext,
                                    "No buttons added",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                            return@ExtendedFloatingActionButton
                        }
                        if (editingMacro)
                            viewModel.updateMacro(
                                MacroDataDBModel(
                                    id = macroDataDBModel.id,
                                    name = macroName,
                                    macroUnits = allMacroUnits
                                )
                            )
                        else
                            viewModel.addMacro(
                                MacroDataDBModel(
                                    id = 0,
                                    name = macroName,
                                    macroUnits = allMacroUnits
                                )
                            )
                        finish()
                    },
                    icon = {
                        Icon(imageVector = Icons.Outlined.Save, contentDescription = "Save")
                    }
                )
            }
        ) {
            val reorderableState = rememberReorderableState(
                listState = androidx.compose.foundation.lazy.rememberLazyListState(),
                onMove = { from, to ->
                    allMacroUnits.add(to, allMacroUnits.removeAt(from))
                }
            )

            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize() 
            ) {
                // Improved Header
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .fillMaxWidth()
                ) {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = macroName,
                        onValueChange = { newValue ->
                            macroName = newValue
                        },
                        placeholder = {
                            Text(
                                text = "Macro Name",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        },
                        textStyle = MaterialTheme.typography.headlineSmall,
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        )
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = reorderableState.listState
                ) {
                    items(allMacroUnits.size) { i ->
                        val unit = allMacroUnits[i]
                        val remote = allRemotes.find { it.id == unit.sourceRemoteId }

                        Box(
                            modifier = Modifier
                                .draggedItem(reorderableState, i)
                                .fillMaxWidth()
                        ) {
                            MacroUnitComposable(
                                macroTransmit = unit,
                                remoteData = remote,
                                onClick = {
                                    index = i
                                    dialogVisible = true
                                },
                                onDelete = {
                                    allMacroUnits.removeAt(i)
                                },
                                dragModifier = Modifier.detectReorder(reorderableState, i)
                            )
                        }
                    }
                    
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            FilledTonalButton(onClick = {
                                index = -1
                                dialogVisible = true
                            }) {
                                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                Text("Add Command")
                            }
                        }
                    }
                }
            }

            // Dialogs
            if (dialogVisible)
                AddMacroUnitDialog(
                    viewModel = viewModel,
                    macroTransmit = if (index >= 0 && index < allMacroUnits.size) allMacroUnits[index] else null,
                    onDismiss = {
                        dialogVisible = false
                    },
                    onDelete = {
                        allMacroUnits.remove(allMacroUnits[index])
                        dialogVisible = false
                    },
                    onAdd = { macroTransmit ->
                        allMacroUnits.add(macroTransmit)
                        dialogVisible = false
                    },
                    onUpdate = { macroTransmit, index ->
                        allMacroUnits.removeAt(index)
                        allMacroUnits.add(index, macroTransmit)
                        dialogVisible = false
                    },
                    index = index
                )
                
            if (deleteConfirmationVisible) {
                AlertDialog(
                    onDismissRequest = { deleteConfirmationVisible = false },
                    title = { Text(text = "Delete Macro?") },
                    text = { Text(text = "Are you sure you want to delete this macro? This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            onClick = {
                                viewModel.deleteMacro(macroDataDBModel)
                                finish()
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { deleteConfirmationVisible = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

        }
    }
}
