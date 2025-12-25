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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.monuk7735.nope.remote.composables.ActionButton
import com.monuk7735.nope.remote.composables.AddMacroUnitDialog
import com.monuk7735.nope.remote.composables.MacroUnitComposable
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
            NopeRemoteTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AddEditMacroRoot(macroDataDBModel)
                }
            }
        }
    }

    @Composable
    fun AddEditMacroRoot(macroDataDBModel: MacroDataDBModel) {

        val allMacroUnits: MutableList<MacroTransmit> = remember {
            mutableStateListOf()
        }

        allMacroUnits.addAll(macroDataDBModel.macroUnits)

        var index: Int by remember {
            mutableStateOf(0)
        }

        var dialogVisible by remember {
            mutableStateOf(false)
        }

        var macroName by remember {
            mutableStateOf(macroDataDBModel.name)
        }

        Scaffold(
            topBar = {
                com.monuk7735.nope.remote.composables.AppBar(
                    title = if (editingMacro) "Edit Macro" else "Add Macro",
                    onBack = {
                        finish()
                    }
                ) {
                    if (editingMacro)
                        ActionButton(
                            name = "Delete",
                            icon = Icons.Outlined.Delete,
                            onClick = {
                                viewModel.deleteMacro(
                                    macroDataDBModel
                                )
                                finish()
                            })
                    ActionButton(
                        name = "Save",
                        icon = Icons.Outlined.Done,
                        onClick = {
                            if (macroName.isEmpty()) {
                                Toast
                                    .makeText(
                                        applicationContext,
                                        "Empty macro name",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                                return@ActionButton
                            }
                            if (allMacroUnits.size == 0) {
                                Toast
                                    .makeText(
                                        applicationContext,
                                        "No buttons added",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                                return@ActionButton
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
                        }
                    )
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = {
                        Text(text = "Add Button")
                    },
                    onClick = {
                        index = -1
                        dialogVisible = true
                    },
                    icon = {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add")
                    }
                )
            }
        ) {
            LazyColumn(
                modifier = Modifier.padding(it)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .fillMaxWidth(),
                            value = macroName,
                            onValueChange = { newValue ->
                                macroName = newValue
                            },
                            label = {
                                Text(
                                    text = "Macro Name",
                                )
                            },
                            singleLine = true,
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.Edit, contentDescription = "")
                            },
                        )
                    }
                }
                items(allMacroUnits.size) { i ->
                    MacroUnitComposable(
                        text = allMacroUnits[i].name,
                        index = i,
                        size = allMacroUnits.size,
                        onClick = {
                            index = i
                            dialogVisible = true
                        },
                        onMoveDown = {
                            allMacroUnits.add(i + 1, allMacroUnits.removeAt(i))
                        },
                        onMoveUp = {
                            allMacroUnits.add(i - 1, allMacroUnits.removeAt(i))
                        }
                    )
                }

            }

            // Dialog
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

        }
    }
}
