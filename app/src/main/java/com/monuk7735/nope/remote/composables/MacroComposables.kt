package com.monuk7735.nope.remote.composables

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.monuk7735.nope.remote.models.custom.macros.MacroTransmit
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.viewmodels.AddEditMacroViewModel

@Composable
fun MacroUnitComposable(
    macroTransmit: MacroTransmit,
    remoteData: RemoteDataDBModel?,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    dragModifier: Modifier = Modifier // New modifier for drag handle
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .padding(5.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 20.dp, top = 10.dp, bottom = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = macroTransmit.sourceButtonName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (remoteData != null) {
                    Text(
                        text = "${remoteData.brand} ${remoteData.type}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = remoteData.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    Text(
                        text = "Unknown Remote",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .clickable { onDelete() }
                        .padding(15.dp),
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
                
                // Drag Handle
                Icon(
                    modifier = dragModifier
                        .padding(15.dp),
                    imageVector = Icons.Outlined.DragHandle,
                    contentDescription = "Drag to Reorder",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun AddMacroUnitDialog(
    viewModel: AddEditMacroViewModel,
    macroTransmit: MacroTransmit?,
    index: Int,
    onAdd: (macroTransmit: MacroTransmit) -> Unit,
    onUpdate: (macroTransmit: MacroTransmit, index: Int) -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    val allRemotes = viewModel.allRemotes.observeAsState().value

    var selectedRemoteDB: RemoteDataDBModel? by remember {
        mutableStateOf(
            if (macroTransmit != null) viewModel.getRemote(macroTransmit.sourceRemoteId)
            else null
        )
    }

    var selectedButton: RemoteButtonDBModel? by remember {
        mutableStateOf(
            if (macroTransmit != null && selectedRemoteDB != null)
                selectedRemoteDB!!.getByName(macroTransmit.sourceButtonName)
            else null
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(),
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (macroTransmit != null) "Edit Command" else "Add Command",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Remote Selection
                Column {
                    Text(
                        text = "Remote",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box {
                        var remoteNameExpanded by remember {
                            mutableStateOf(false)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { remoteNameExpanded = !remoteNameExpanded }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedRemoteDB?.name ?: "Select Remote",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Outlined.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            modifier = Modifier,
                            expanded = remoteNameExpanded,
                            onDismissRequest = { remoteNameExpanded = false })
                        {
                            allRemotes?.forEach {
                                DropdownMenuItem(
                                    onClick = {
                                        remoteNameExpanded = false
                                        selectedRemoteDB = it
                                        selectedButton = null
                                    },
                                    text = { Text(text = it.name) }
                                )
                            }
                        }
                    }
                }

                // Button Selection
                Column {
                    Text(
                        text = "Button",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box {
                        var isButtonNameExpanded by remember {
                            mutableStateOf(false)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isButtonNameExpanded = !isButtonNameExpanded }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedButton?.name ?: "Select Button",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Outlined.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            modifier = Modifier,
                            expanded = isButtonNameExpanded,
                            onDismissRequest = { isButtonNameExpanded = false })
                        {
                            selectedRemoteDB?.onScreenRemoteButtonDBS?.forEach {
                                DropdownMenuItem(
                                    onClick = {
                                        isButtonNameExpanded = false
                                        selectedButton = it
                                    },
                                    text = { Text(text = it.name) }
                                )
                            }
                            selectedRemoteDB?.offScreenRemoteButtonDBS?.forEach {
                                DropdownMenuItem(
                                    onClick = {
                                        isButtonNameExpanded = false
                                        selectedButton = it
                                    },
                                    text = { Text(text = it.name) }
                                )
                            }
                        }
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (macroTransmit != null) {
                        androidx.compose.material3.TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(text = "Delete")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    androidx.compose.material3.TextButton(
                        onClick = onDismiss
                    ) {
                        Text(text = "Cancel")
                    }

                    androidx.compose.material3.TextButton(
                        onClick = {
                            val tempSelectedRemote = selectedRemoteDB
                            val tempSelectedButton = selectedButton
                            if (tempSelectedRemote == null || tempSelectedButton == null)
                                Toast.makeText(
                                    viewModel.getApplication(),
                                    "Select a Button",
                                    Toast.LENGTH_SHORT
                                ).show()
                            else {
                                if (macroTransmit != null) {
                                    onUpdate(
                                        MacroTransmit(
                                            name = "${tempSelectedRemote.brand}-${tempSelectedButton.name}",
                                            irPattern = tempSelectedButton.irPattern,
                                            sourceRemoteId = tempSelectedRemote.id,
                                            sourceButtonName = tempSelectedButton.name
                                        ),
                                        index
                                    )
                                } else
                                    onAdd(
                                        MacroTransmit(
                                            name = "${tempSelectedRemote.brand}-${tempSelectedButton.name}",
                                            irPattern = tempSelectedButton.irPattern,
                                            sourceRemoteId = tempSelectedRemote.id,
                                            sourceButtonName = tempSelectedButton.name
                                        )
                                    )
                            }
                        }
                    ) {
                        Text(text = if (macroTransmit != null) "Update" else "Add")
                    }
                }
            }
        }
    }
}
