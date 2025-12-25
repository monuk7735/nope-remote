package com.monuk7735.nope.remote.composables

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
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
    text: String,
    index: Int,
    size: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .padding(5.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text)
            Column {
                Icon(
                    modifier = Modifier
                        .clickable(index > 0) { onMoveUp() }
                        .fillMaxHeight(0.5f)
                        .padding(15.dp),
                    imageVector = Icons.Outlined.KeyboardArrowUp,
                    contentDescription = "Move Up"
                )
                Icon(
                    modifier = Modifier
                        .clickable(index + 1 < size) { onMoveDown() }
                        .fillMaxHeight(0.5f)
                        .padding(15.dp),
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Move Down"
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
        Card {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Remote")
                    Box {
                        var remoteNameExpanded by remember {
                            mutableStateOf(false)
                        }
                        Row(
                            modifier = Modifier
                                .clickable { remoteNameExpanded = !remoteNameExpanded }
                                .padding(15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(text = selectedRemoteDB?.name ?: "Remote")
                            Icon(
                                imageVector = Icons.Outlined.ArrowDropDown,
                                contentDescription = ""
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Button")
                    Box {
                        var isButtonNameExpanded by remember {
                            mutableStateOf(false)
                        }
                        Row(
                            modifier = Modifier
                                .clickable { isButtonNameExpanded = !isButtonNameExpanded }
                                .padding(15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(text = selectedButton?.name ?: "Button")
                            Icon(
                                imageVector = Icons.Outlined.ArrowDropDown,
                                contentDescription = ""
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
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (macroTransmit != null)
                        ElevatedButton(
                            modifier = Modifier,
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(text = "Delete")
                        }

                    ElevatedButton(
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
                        Text(text = "Done")
                    }
                }
            }
        }
    }
}
