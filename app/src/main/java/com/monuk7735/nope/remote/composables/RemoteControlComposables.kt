package com.monuk7735.nope.remote.composables

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Composable
fun RemoteControl(
    remoteDataModel: RemoteDataDBModel?,
    onEditRemoteSettings: () -> Unit,
    onBack: () -> Unit
) {
    var layoutLimits by remember {
        mutableStateOf(Rect(Offset.Zero, 0f))
    }

    Scaffold(
        topBar = {
            AppBar(
                title = "${remoteDataModel?.name}",
                onBack = onBack,
                actions = {
                    ActionButton(
                        name = "Settings",
                        icon = Icons.Outlined.Settings,
                        onClick = {
                            onEditRemoteSettings()
                        }
                    )
                }
            )
        },
        content = {
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        layoutLimits = coordinates.boundsInWindow()
                    }
                    .background(MaterialTheme.colorScheme.background)
            ) {
                UniversalRemote(
                    remoteDataDBModel = remoteDataModel,
                    layoutLimits = layoutLimits
                )
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
    onSaveLayout: (remoteDataModel: RemoteDataDBModel) -> Unit,
    onBack: () -> Unit
) {

    var localRemoteDataDBModel = remoteDataModel?.copy()

    var layoutLimits by remember {
        mutableStateOf(Rect(Offset.Zero, 0f))
    }

    Scaffold(
        topBar = {
            AppBar(
                title = "Editing ${remoteDataModel?.name}",
                onBack = onBack,
                actions = {
                    ActionButton(
                        name = "Save",
                        icon = Icons.Outlined.Done,
                        onClick = {
                            val temp = localRemoteDataDBModel
                            if (temp != null)
                                onSaveLayout(temp)
                        }
                    )
                }
            )
        },
        content = {
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        layoutLimits = coordinates.boundsInWindow()
                    }
            ) {
                EditableRemote(
                    remoteDataDBModel = remoteDataModel,
                    onUpdate = { dbModel ->
                        localRemoteDataDBModel = dbModel
                    },
                    layoutLimits = layoutLimits
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
    var remoteName by remember {
        mutableStateOf(remoteDataModel?.name ?: "")
    }
    val errorToast = Toast.makeText(
        LocalContext.current,
        "Remote Name cannot be empty",
        Toast.LENGTH_SHORT
    )
    Scaffold(
        topBar = {
            AppBar(
                title = "${remoteDataModel?.name} Settings",
                onBack = onBack,
                actions = {
                    ActionButton(
                        name = "",
                        icon = Icons.Outlined.Done,
                        onClick = {
                            if (remoteDataModel == null)
                                return@ActionButton
                            if (remoteName.isEmpty()) {
                                errorToast.show()
                                return@ActionButton
                            }
                            onSave(
                                remoteDataModel.copy(
                                    name = remoteName
                                )
                            )
                        }
                    )
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()

                var deleteDialogVisible by remember {
                    mutableStateOf(false)
                }

                OutlinedTextField(
                    modifier = modifier,
                    value = remoteName,
                    onValueChange = {
                        remoteName = it
                    },
                    label = {
                        Text(text = "Remote Name")
                    }
                )
                Button(
                    modifier = modifier,
                    onClick = {
                        onEditLayout()
                    }
                ) {
                    Text(
                        modifier = Modifier.padding(5.dp),
                        text = "Edit Remote Layout"
                    )
                }

                Button(
                    modifier = modifier,
                    onClick = {
                        deleteDialogVisible = true;
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        modifier = Modifier.padding(5.dp),
                        text = "Delete Remote"
                    )
                }

                if (deleteDialogVisible)
                    Dialog(
                        onDismissRequest = {
                            deleteDialogVisible = false
                        }
                    ) {
                        Card {
                            Column(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .width(200.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Are you sure?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = MaterialTheme.typography.titleLarge.fontSize
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        if (remoteDataModel != null)
                                            onDelete(remoteDataModel)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text(
                                        text = "Yes",
                                    )
                                }
                            }
                        }
                    }
            }
        }
    )
}