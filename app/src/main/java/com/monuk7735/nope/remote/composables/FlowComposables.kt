package com.monuk7735.nope.remote.composables

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.monuk7735.nope.remote.models.custom.flows.FlowTransmit
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.viewmodels.AddEditFlowViewModel

@Composable
fun FlowUnitComposable(
    text: String,
    index: Int,
    size: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .padding(5.dp),
        backgroundColor = MaterialTheme.colorScheme.surface,
        elevation = 2.dp
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
                        .clickable
                            (index > 0) {
                            onMoveUp()
                        }
                        .fillMaxHeight(0.5f)
                        .padding(15.dp),
                    imageVector = Icons.Outlined.KeyboardArrowUp,
                    contentDescription = "Move Up"
                )
                Icon(
                    modifier = Modifier
                        .clickable
                            (index + 1 < size) {
                            onMoveDown()
                        }
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
fun AddFlowUnitDialog(
    viewModel: AddEditFlowViewModel,
    flowTransmit: FlowTransmit?,
    index: Int,
    onAdd: (flowTransmit: FlowTransmit) -> Unit,
    onUpdate: (flowTransmit: FlowTransmit, index: Int) -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    val allRemotes = viewModel.allRemotes.observeAsState().value

    var selectedRemoteDB: RemoteDataDBModel? by remember {
        mutableStateOf(
            if (flowTransmit != null) viewModel.getRemote(flowTransmit.sourceRemoteId)
            else null
        )
    }

    var selectedButton: RemoteButtonDBModel? by remember {
        mutableStateOf(
            if (flowTransmit != null && selectedRemoteDB != null)
                selectedRemoteDB!!.getByName(
                    flowTransmit.sourceButtonName
                )
            else null
        )
    }

    Dialog(
        onDismissRequest = {
            onDismiss()
        },
        properties = DialogProperties(

        ),
    ) {
        Card {
            Column(
                modifier = Modifier
//                .background(
//                    MaterialTheme.colors.surface,
//                    RoundedCornerShape(4.dp)
//                )
                    .padding(20.dp)
            ) {
//            val allRemotes = viewModel.allRemotes.observeAsState().value
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
                                .clickable {
                                    remoteNameExpanded = !remoteNameExpanded
                                }
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
                            onDismissRequest = {
                                remoteNameExpanded = false
                            }) {
                            allRemotes?.forEach {
                                DropdownMenuItem(onClick = {
                                    remoteNameExpanded = false
                                    selectedRemoteDB = it
                                    selectedButton = null
                                }) {
                                    Text(text = it.name)
                                }
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
                                .clickable {
                                    isButtonNameExpanded = !isButtonNameExpanded
                                }
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
                            onDismissRequest = {
                                isButtonNameExpanded = false
                            }) {
                            selectedRemoteDB?.onScreenRemoteButtonDBS?.forEach {
                                DropdownMenuItem(onClick = {
                                    isButtonNameExpanded = false
                                    selectedButton = it
                                }) {
                                    Text(text = it.name)
                                }
                            }
                            selectedRemoteDB?.offScreenRemoteButtonDBS?.forEach {
                                DropdownMenuItem(onClick = {
                                    isButtonNameExpanded = false
                                    selectedButton = it
                                }) {
                                    Text(text = it.name)
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (flowTransmit != null)
                        ElevatedButton(
                            modifier = Modifier,
                            onClick = {
                                onDelete()
                            },
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
                                if (flowTransmit != null) {
                                    onUpdate(
                                        FlowTransmit(
                                            name = "${tempSelectedRemote.brand}-${tempSelectedButton.name}",
                                            irPattern = tempSelectedButton.irPattern,
                                            sourceRemoteId = tempSelectedRemote.id,
                                            sourceButtonName = tempSelectedButton.name
                                        ),
                                        index
                                    )
                                } else
                                    onAdd(
                                        FlowTransmit(
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
