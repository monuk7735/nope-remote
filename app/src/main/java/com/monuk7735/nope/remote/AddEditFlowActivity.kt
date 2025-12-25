package com.monuk7735.nope.remote

import android.os.Bundle
import android.util.Log
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
import com.monuk7735.nope.remote.composables.AddFlowUnitDialog
import com.monuk7735.nope.remote.composables.FlowUnitComposable
import com.monuk7735.nope.remote.models.custom.flows.FlowTransmit
import com.monuk7735.nope.remote.models.database.FlowDataDBModel
import com.monuk7735.nope.remote.ui.theme.NopeRemoteTheme
import com.monuk7735.nope.remote.viewmodels.AddEditFlowViewModel

@ExperimentalMaterial3Api
class AddEditFlowActivity : ComponentActivity() {

    private lateinit var viewModel: AddEditFlowViewModel

    private var editingFlow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[AddEditFlowViewModel::class.java]

        val flowDataDBModel: FlowDataDBModel =
            intent.getParcelableExtra("flow_data") ?: FlowDataDBModel(0, "", listOf())

        editingFlow = flowDataDBModel.id != 0

        setContent {
            NopeRemoteTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AddEditRemoteRoot(flowDataDBModel)
                }
            }
        }
    }

    @Composable
    fun AddEditRemoteRoot(flowDataDBModel: FlowDataDBModel) {

        val allFlowUnits: MutableList<FlowTransmit> = remember {
            mutableStateListOf()
        }

        Log.d("monumonu", "AddEditRemoteRoot: Editing ${flowDataDBModel.name}")

        allFlowUnits.addAll(flowDataDBModel.flowUnits)

        var index: Int by remember {
            mutableStateOf(0)
        }

        var dialogVisible by remember {
            mutableStateOf(false)
        }

        var flowName by remember {
            mutableStateOf(flowDataDBModel.name)
        }

        Scaffold(
            topBar = {
                com.monuk7735.nope.remote.composables.AppBar(
                    title = stringResource(id = R.string.app_name)
                ) {
                    if (editingFlow)
                        ActionButton(
                            name = "Delete",
                            icon = Icons.Outlined.Delete,
                            onClick = {
                                viewModel.deleteFlow(
                                    flowDataDBModel
                                )
                                finish()
                            })
                    ActionButton(
                        name = "Save",
                        icon = Icons.Outlined.Done,
                        onClick = {
                            if (flowName.isEmpty()) {
                                Toast
                                    .makeText(
                                        applicationContext,
                                        "Empty flow name",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                                return@ActionButton
                            }
                            if (allFlowUnits.size == 0) {
                                Toast
                                    .makeText(
                                        applicationContext,
                                        "No buttons added",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                                return@ActionButton
                            }
                            if (editingFlow)
                                viewModel.updateFlow(
                                    FlowDataDBModel(
                                        id = flowDataDBModel.id,
                                        name = flowName,
                                        flowUnits = allFlowUnits
                                    )
                                )
                            else
                                viewModel.addFlow(
                                    FlowDataDBModel(
                                        id = 0,
                                        name = flowName,
                                        flowUnits = allFlowUnits
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
                            value = flowName,
                            onValueChange = { newValue ->
                                flowName = newValue
                            },
                            label = {
                                Text(
                                    text = "Flow Name",
                                )
                            },
                            singleLine = true,
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.Edit, contentDescription = "")
                            },
                        )
                    }
                }
                items(allFlowUnits.size) { i ->
                    FlowUnitComposable(
                        text = allFlowUnits[i].name,
                        index = i,
                        size = allFlowUnits.size,
                        onClick = {
                            index = i
                            dialogVisible = true
                        },
                        onMoveDown = {
                            allFlowUnits.add(i + 1, allFlowUnits.removeAt(i))
                        },
                        onMoveUp = {
                            allFlowUnits.add(i - 1, allFlowUnits.removeAt(i))
                        }
                    )
                }

            }

            // Dialog
            if (dialogVisible)
                AddFlowUnitDialog(
                    viewModel = viewModel,
                    flowTransmit = if (index >= 0 && index < allFlowUnits.size) allFlowUnits[index] else null,
                    onDismiss = {
                        dialogVisible = false
                    },
                    onDelete = {
                        allFlowUnits.remove(allFlowUnits[index])
                        dialogVisible = false
                    },
                    onAdd = { flowTransmit ->
                        allFlowUnits.add(flowTransmit)
                        dialogVisible = false
                    },
                    onUpdate = { flowTransmit, index ->
                        allFlowUnits.removeAt(index)
                        allFlowUnits.add(index, flowTransmit)
                        dialogVisible = false
                    },
                    index = index
                )

        }
    }
}
