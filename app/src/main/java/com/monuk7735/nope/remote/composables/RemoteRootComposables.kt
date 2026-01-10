package com.monuk7735.nope.remote.composables

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun EditableRemote(
        remoteDataDBModel: RemoteDataDBModel?,
        onUpdate: (remoteDataDBModel: RemoteDataDBModel) -> Unit,
        layoutLimits: Rect,
        gridEnabled: Boolean = false,
        hCount: Int = 5,
        vCount: Int = 10,
        textOnlyMode: Boolean = false
) {

    val onScreenRemotes: MutableList<RemoteButtonDBModel> =
            remember(remoteDataDBModel) {
                if (remoteDataDBModel != null)
                        mutableStateListOf(
                                elements = remoteDataDBModel.onScreenRemoteButtonDBS.toTypedArray()
                        )
                else mutableStateListOf()
            }

    val offScreenRemotes: MutableList<RemoteButtonDBModel> =
            remember(remoteDataDBModel) {
                if (remoteDataDBModel != null)
                        mutableStateListOf(
                                elements = remoteDataDBModel.getAllOffScreen().toTypedArray()
                        )
                else mutableStateListOf()
            }

    val gridSizeXPx = if (layoutLimits.width > 0) layoutLimits.width / hCount.toFloat() else 0f
    val gridSizeYPx = if (layoutLimits.height > 0) layoutLimits.height / vCount.toFloat() else 0f
    var buttonSize = LocalDensity.current.run { 68.dp.toPx() }

    if (gridEnabled && gridSizeXPx > 0 && gridSizeYPx > 0) {
        Canvas(modifier = Modifier.fillMaxSize()) {

            for (i in 0..hCount) {
                val x = i * gridSizeXPx
                drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height)
                )
            }
            for (i in 0..vCount) {
                val y = i * gridSizeYPx
                drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y)
                )
            }
        }
    }

    onScreenRemotes.forEachIndexed { index, remoteButton ->
        key(remoteButton.id) {
            RemoteButtonSingleEditable(
                    name = remoteButton.name,
                    icon = remoteButton.getIcon(),
                    textIcon = remoteButton.getTextIcon(),
                    forceTextOnly = textOnlyMode,
                    offsetX = remoteButton.offsetX,
                    offsetY = remoteButton.offsetY,
                    layoutLimits = layoutLimits,
                    onPosUpdate = { posX, posY ->
                        var snappedX =
                                if (gridEnabled && gridSizeXPx > 0) {
                                    val steps = round(posX / gridSizeXPx)
                                    gridSizeXPx * steps + (gridSizeXPx - buttonSize) / 2
                                } else posX

                        var snappedY =
                                if (gridEnabled && gridSizeYPx > 0) {
                                    val steps = round(posY / gridSizeYPx)
                                    gridSizeYPx * steps + (gridSizeYPx - buttonSize) / 2
                                } else posY

                        onScreenRemotes[index] =
                                onScreenRemotes[index].copy(offsetX = snappedX, offsetY = snappedY)

                        if (remoteDataDBModel != null)
                                onUpdate(
                                        remoteDataDBModel.copy(
                                                onScreenRemoteButtonDBS = onScreenRemotes,
                                                offScreenRemoteButtonDBS = offScreenRemotes
                                        )
                                )
                    },
                    onRemove = { offScreenRemotes.add(onScreenRemotes.removeAt(index)) }
            )
        }
    }
    RemoteButtonExtra(
            extraButtons = offScreenRemotes,
            offsetX =
                    LocalDensity.current.run {
                        (layoutLimits.right - layoutLimits.left).toDp() - 80.dp
                    },
            offsetY =
                    LocalDensity.current.run {
                        (layoutLimits.bottom - layoutLimits.top).toDp() - 80.dp
                    },
            size = 80.dp,
            onClick = {
                onScreenRemotes.add(it)
                offScreenRemotes.remove(it)
            }
    )
    RemoteButtonDigits(
            digitButtons = remoteDataDBModel?.getAllDigits(),
            offsetX = 0.dp,
            offsetY =
                    LocalDensity.current.run {
                        (layoutLimits.bottom - layoutLimits.top).toDp() - 80.dp
                    },
            size = 80.dp,
            onClick = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@Composable
fun UniversalRemote(
        remoteDataDBModel: RemoteDataDBModel?,
        layoutLimits: Rect,
        textOnlyMode: Boolean = false
) {
    val irController =
            LocalContext.current.run {
                val manager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
                if (manager != null) IRController(manager) else null
            }

    val vibrator =
            LocalContext.current.run {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager =
                            getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    getSystemService(Vibrator::class.java)!!
                }
            }

    Box(modifier = Modifier.fillMaxSize()) {
        remoteDataDBModel?.onScreenRemoteButtonDBS?.forEach {
            RemoteButtonSingle(
                    name = it.name,
                    icon = it.getIcon(),
                    textIcon = it.getTextIcon(),
                    forceTextOnly = textOnlyMode,
                    offsetX = it.offsetX,
                    offsetY = it.offsetY,
                    onClick = { it.transmit(irController, vibrator) }
            )
        }
        RemoteButtonExtra(
                extraButtons = remoteDataDBModel?.getAllOffScreen(),
                offsetX =
                        LocalDensity.current.run {
                            (layoutLimits.right - layoutLimits.left).toDp() - 80.dp
                        },
                offsetY =
                        LocalDensity.current.run {
                            (layoutLimits.bottom - layoutLimits.top).toDp() - 80.dp
                        },
                size = 80.dp,
                onClick = { it.transmit(irController, vibrator) }
        )
        RemoteButtonDigits(
                digitButtons = remoteDataDBModel?.getAllDigits(),
                offsetX = 0.dp,
                offsetY =
                        LocalDensity.current.run {
                            (layoutLimits.bottom - layoutLimits.top).toDp() - 80.dp
                        },
                size = 80.dp,
                onClick = { it.transmit(irController, vibrator) }
        )
    }
}


