package com.monuk7735.nope.remote.composables

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Vibrator
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun EditableRemote(
    remoteDataDBModel: RemoteDataDBModel?,
    onUpdate: (remoteDataDBModel: RemoteDataDBModel) -> Unit,
    layoutLimits: Rect,
) {

    val onScreenRemotes: MutableList<RemoteButtonDBModel> = remember {
        if (remoteDataDBModel != null)
            mutableStateListOf( elements = remoteDataDBModel.onScreenRemoteButtonDBS.toTypedArray())
        else
        mutableStateListOf()
    }

    val offScreenRemotes: MutableList<RemoteButtonDBModel> = remember {
        if (remoteDataDBModel != null)
            mutableStateListOf( elements = remoteDataDBModel.getAllOffScreen().toTypedArray())
        else
            mutableStateListOf()
    }

    onScreenRemotes.forEachIndexed { index, remoteButton ->
        RemoteButtonSingleEditable(
            name = remoteButton.name,
            icon = remoteButton.getIcon(),
            offsetX = remoteButton.offsetX,
            offsetY = remoteButton.offsetY,
            onPosUpdate = { posX, posY ->
                onScreenRemotes[index].offsetX = posX
                onScreenRemotes[index].offsetY = posY
                if (remoteDataDBModel != null)
                    onUpdate(remoteDataDBModel.copy(
                        onScreenRemoteButtonDBS = onScreenRemotes,
                        offScreenRemoteButtonDBS = offScreenRemotes
                    ))
            },
            layoutLimits = layoutLimits,
            onRemove = {
                offScreenRemotes.add(onScreenRemotes.removeAt(index))
            }
        )
    }
    RemoteButtonExtra(
        extraButtons = offScreenRemotes,
        offsetX = LocalDensity.current.run { (layoutLimits.right - layoutLimits.left).toDp() - 80.dp },
        offsetY = LocalDensity.current.run { (layoutLimits.bottom - layoutLimits.top).toDp() - 80.dp },
        size = 80.dp,
        onClick = {
            onScreenRemotes.add(it)
            offScreenRemotes.remove(it)
        }
    )
    RemoteButtonDigits(
        digitButtons = remoteDataDBModel?.getAllDigits(),
        offsetX = 0.dp,
        offsetY = LocalDensity.current.run { (layoutLimits.bottom - layoutLimits.top).toDp() - 80.dp },
        size = 80.dp,
        onClick = {

        }
    )
}

@ExperimentalFoundationApi
@Composable
fun UniversalRemote(
    remoteDataDBModel: RemoteDataDBModel?,
    layoutLimits: Rect,
) {
    val irController = LocalContext.current.run {
        IRController(getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager)
    }

    val vibrator = LocalContext.current.run {
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        remoteDataDBModel?.onScreenRemoteButtonDBS?.forEach {
            RemoteButtonSingle(
                name = it.name,
                icon = it.getIcon(),
                offsetX = it.offsetX,
                offsetY = it.offsetY,
                onClick = {
                    it.transmit(
                        irController,
                        vibrator
                    )
                }
            )
        }
        RemoteButtonExtra(
            extraButtons = remoteDataDBModel?.getAllOffScreen(),
            offsetX = LocalDensity.current.run { (layoutLimits.right - layoutLimits.left).toDp() - 80.dp },
            offsetY = LocalDensity.current.run { (layoutLimits.bottom - layoutLimits.top).toDp() - 80.dp },
            size = 80.dp,
            onClick = {
                it.transmit(irController, vibrator)
            }
        )
        RemoteButtonDigits(
            digitButtons = remoteDataDBModel?.getAllDigits(),
            offsetX = 0.dp,
            offsetY = LocalDensity.current.run { (layoutLimits.bottom - layoutLimits.top).toDp() - 80.dp },
            size = 80.dp,
            onClick = {
                it.transmit(irController, vibrator)
            }
        )
    }
}

//@ExperimentalComposeUiApi
//@ExperimentalFoundationApi
//@Composable
//fun TVRemote(
//    modifier: Modifier = Modifier,
//    remoteDataModel: RemoteDataModel,
//    irController: IRController,
//) {
//    val (showDigitsDialog, setShowDigitsDialog) = remember {
//        mutableStateOf(false)
//    }
//    val (showExtrasDialog, setShowExtrasDialog) = remember {
//        mutableStateOf(false)
//    }
//    Column(
//        modifier = modifier
//            .padding(30.dp)
//            .fillMaxSize(1f),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        val rowModifier = Modifier
//            .padding(10.dp)
//            .fillMaxWidth()
//        Row(
//            modifier = rowModifier,
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            RemoteButtonSingle(
//                name = "Power",
//                remoteButtonModel = remoteDataModel.getByName("Power Toggle", irController)
//            )
//            RemoteButtonSingle(
//                name = "Exit",
//                remoteButtonModel = remoteDataModel.getByName("Exit", irController)
//            )
//            RemoteButtonSingle(
//                name = "Menu",
//                remoteButtonModel = remoteDataModel.getByName("Menu", irController)
//            )
//        }
//        Row(
//            modifier = rowModifier,
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            RemoteButtonSingle(
//                name = "Home",
//                remoteButtonModel = remoteDataModel.getByName("Home", irController)
//            )
//            RemoteButtonExtra(
//                icon = Icons.Outlined.Edit,
//                name = "Digits",
//                onClick = {
//                    setShowDigitsDialog(true)
//                }
//            )
//            RemoteButtonSingle(
//                name = "Back",
//                remoteButtonModel = remoteDataModel.getByName("Back", irController)
//            )
//        }
//        RemoteButtonDPad(
//            modifier = rowModifier.weight(1f),
//            okRemoteButtonModel = remoteDataModel.getByName("Cursor Enter", irController),
//            upRemoteButtonModel = remoteDataModel.getByName("Cursor Up", irController),
//            downRemoteButtonModel = remoteDataModel.getByName("Cursor Down", irController),
//            leftRemoteButtonModel = remoteDataModel.getByName("Cursor Left", irController),
//            rightRemoteButtonModel = remoteDataModel.getByName("Cursor Right", irController)
//        )
//        Row(
//            modifier = rowModifier,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            RemoteButtonPairUpDown(
////                modifier = rowElementModifier,
//                buttonName = "CH",
//                upRemoteButtonModel = remoteDataModel.getByName("Channel Up", irController),
//                downRemoteButtonModel = remoteDataModel.getByName("Channel Down", irController)
//            )
//            RemoteButtonPairUpDown(
////                modifier = rowElementModifier,
//                buttonName = "VOL",
//                upRemoteButtonModel = remoteDataModel.getByName("Volume Up", irController),
//                downRemoteButtonModel = remoteDataModel.getByName("Volume Down", irController)
//            )
//        }
//        Row(
//            modifier = rowModifier,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            RemoteButtonExtra(
//                icon = Icons.Outlined.Star,
//                name = "Extra Buttons",
//                onClick = {
//                    setShowExtrasDialog(true)
//                }
//            )
//            RemoteButtonSingle(
//                name = "Mute",
//                remoteButtonModel = remoteDataModel.getByName("Mute Toggle", irController)
//            )
//        }
//    }
//    if (showDigitsDialog) {
//        Dialog(
//            properties = DialogProperties(
//                dismissOnBackPress = true,
//                dismissOnClickOutside = true,
//            ),
//            onDismissRequest = {
//                setShowDigitsDialog(false)
//            }
//        ) {
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//            ) {
//                LazyVerticalGrid(cells = GridCells.Fixed(4), content = {
//                    val digitsRemotes = remoteDataModel.getDigits(irController)
//                    if (digitsRemotes.isEmpty())
//                        setShowDigitsDialog(false)
//                    items(digitsRemotes.size) { i ->
//                        RemoteButtonOverflow(
//                            remoteButtonModel = digitsRemotes[i]
//                        )
//                    }
//                })
//            }
//        }
//    }
//    if (showExtrasDialog) {
//        Dialog(
//            properties = DialogProperties(
//                dismissOnBackPress = true,
//                dismissOnClickOutside = true,
//            ),
//            onDismissRequest = {
//                setShowExtrasDialog(false)
//            }
//        ) {
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//            ) {
//                LazyColumn(
//                    modifier = Modifier.padding(30.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    val extraRemoteButtons = remoteDataModel.getExtras(irController)
//                    if (extraRemoteButtons.isEmpty())
//                        setShowExtrasDialog(false)
//                    items(extraRemoteButtons.size) { i ->
//                        RemoteButtonOverflow(
//                            remoteButtonModel = extraRemoteButtons[i]
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//}
