package com.monuk7735.nope.remote.composables

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.monuk7735.nope.remote.models.custom.macros.MacroTransmit
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.viewmodels.AddEditMacroViewModel

@Composable
fun MacroStepItem(
    stepNumber: Int,
    macroTransmit: MacroTransmit,
    remoteData: RemoteDataDBModel?,
    isLast: Boolean,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    dragModifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(84.dp)
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable { onClick() },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = macroTransmit.sourceButtonName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (remoteData != null) {
                            Icon(
                                imageVector = remoteData.getIcon(),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = remoteData.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "Missing Remote",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Remove Step",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Outlined.DragHandle,
                        contentDescription = "Drag to Reorder",
                        modifier = dragModifier
                            .padding(4.dp)
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun MacroUnitComposable(
    macroTransmit: MacroTransmit,
    remoteData: RemoteDataDBModel?,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    dragModifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
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
                        text = "${remoteData.brand} ${remoteData.type} • ${remoteData.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        .padding(8.dp),
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
                
                Icon(
                    modifier = dragModifier
                        .padding(8.dp),
                    imageVector = Icons.Outlined.DragHandle,
                    contentDescription = "Drag to Reorder",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
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
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            ),
            elevation = CardDefaults.cardElevation(8.dp)
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
