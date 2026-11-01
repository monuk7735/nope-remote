package com.monuk7735.nope.remote.composables

import android.content.Context
import android.hardware.ConsumerIrManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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
import com.monuk7735.nope.remote.utils.VibrationHelper

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun EditableRemote(
    remoteDataDBModel: RemoteDataDBModel?,
    onUpdate: (remoteDataDBModel: RemoteDataDBModel) -> Unit,
    layoutLimits: Rect,
    hCount: Int = 5,
    vCount: Int = 10,
    textOnlyMode: Boolean = false,
) {
    val gridSizeXPx = if (layoutLimits.width > 0) layoutLimits.width / hCount.toFloat() else 0f
    val gridSizeYPx = if (layoutLimits.height > 0) layoutLimits.height / vCount.toFloat() else 0f
    val buttonSize = LocalDensity.current.run { 72.dp.toPx() }

    val onScreenRemotes: MutableList<RemoteButtonDBModel> =
        remember(remoteDataDBModel?.id) {
            if (remoteDataDBModel != null) {
                val buttons = remoteDataDBModel.onScreenRemoteButtonDBS.map { btn ->
                    if (btn.gridCol == null || btn.gridRow == null) {
                        val cx = btn.offsetX + buttonSize / 2f
                        val cy = btn.offsetY + buttonSize / 2f
                        val col = if (gridSizeXPx > 0) {
                            (cx / gridSizeXPx).toInt().coerceIn(0, hCount - 1)
                        } else {
                            0
                        }
                        val row = if (gridSizeYPx > 0) {
                            (cy / gridSizeYPx).toInt().coerceIn(0, vCount - 1)
                        } else {
                            0
                        }
                        btn.copy(gridCol = col, gridRow = row)
                    } else {
                        btn
                    }
                }
                mutableStateListOf(elements = buttons.toTypedArray())
            } else {
                mutableStateListOf()
            }
        }

    val offScreenRemotes: MutableList<RemoteButtonDBModel> =
        remember(remoteDataDBModel?.id) {
            if (remoteDataDBModel != null) {
                mutableStateListOf(
                    elements = remoteDataDBModel.getAllOffScreen().toTypedArray(),
                )
            } else {
                mutableStateListOf()
            }
        }

    if (gridSizeXPx > 0 && gridSizeYPx > 0) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0..hCount) {
                val x = i * gridSizeXPx
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                )
            }
            for (i in 0..vCount) {
                val y = i * gridSizeYPx
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                )
            }
        }
    }

    onScreenRemotes.forEachIndexed { index, remoteButton ->
        key(remoteButton.id) {
            val col = remoteButton.gridCol
            val row = remoteButton.gridRow

            val isOutOfBounds = col == null ||
                row == null ||
                col !in 0 until hCount ||
                row !in 0 until vCount
            val isColliding = if (col != null && row != null) {
                onScreenRemotes.count { it.gridCol == col && it.gridRow == row } > 1
            } else {
                false
            }
            val isReservedCorner = (col == 0 && row == vCount - 1) ||
                (col == hCount - 1 && row == vCount - 1)
            val isInvalid = isOutOfBounds || isColliding || isReservedCorner

            val effectiveCol = col ?: if (gridSizeXPx > 0) {
                val cx = remoteButton.offsetX + buttonSize / 2f
                (cx / gridSizeXPx).toInt().coerceIn(0, hCount - 1)
            } else {
                0
            }
            val effectiveRow = row ?: if (gridSizeYPx > 0) {
                val cy = remoteButton.offsetY + buttonSize / 2f
                (cy / gridSizeYPx).toInt().coerceIn(0, vCount - 1)
            } else {
                0
            }

            val buttonDisplayX = if (gridSizeXPx > 0) {
                (gridSizeXPx * effectiveCol + (gridSizeXPx - buttonSize) / 2)
                    .coerceIn(0f, (layoutLimits.width - buttonSize).coerceAtLeast(0f))
            } else {
                remoteButton.offsetX
            }

            val buttonDisplayY = if (gridSizeYPx > 0) {
                (gridSizeYPx * effectiveRow + (gridSizeYPx - buttonSize) / 2)
                    .coerceIn(0f, (layoutLimits.height - buttonSize).coerceAtLeast(0f))
            } else {
                remoteButton.offsetY
            }

            RemoteButtonSingleEditable(
                name = remoteButton.name,
                icon = remoteButton.getIcon(),
                textIcon = remoteButton.getTextIcon(),
                forceTextOnly = textOnlyMode,
                offsetX = buttonDisplayX,
                offsetY = buttonDisplayY,
                layoutLimits = layoutLimits,
                isInvalid = isInvalid,
                onPosUpdate = { posX, posY ->
                    if (gridSizeXPx > 0 && gridSizeYPx > 0) {
                        val centerX = posX + buttonSize / 2f
                        val centerY = posY + buttonSize / 2f
                        val targetCol =
                            (centerX / gridSizeXPx).toInt().coerceIn(0, hCount - 1)
                        val targetRow =
                            (centerY / gridSizeYPx).toInt().coerceIn(0, vCount - 1)

                        val snappedX = gridSizeXPx * targetCol + (gridSizeXPx - buttonSize) / 2
                        val snappedY = gridSizeYPx * targetRow + (gridSizeYPx - buttonSize) / 2

                        val reservedSlots = setOf(0 to vCount - 1, hCount - 1 to vCount - 1)
                        val isTargetReserved = (targetCol == 0 && targetRow == vCount - 1) ||
                            (targetCol == hCount - 1 && targetRow == vCount - 1)

                        val prevCol = remoteButton.gridCol
                        val prevRow = remoteButton.gridRow
                        val prevWasValid = prevCol != null &&
                            prevRow != null &&
                            prevCol in 0 until hCount &&
                            prevRow in 0 until vCount &&
                            !reservedSlots.contains(prevCol to prevRow)

                        if (isTargetReserved) {
                            if (prevWasValid) {
                                val prevX =
                                    gridSizeXPx * prevCol + (gridSizeXPx - buttonSize) / 2
                                val prevY =
                                    gridSizeYPx * prevRow + (gridSizeYPx - buttonSize) / 2
                                onScreenRemotes[index] = onScreenRemotes[index].copy(
                                    gridCol = prevCol,
                                    gridRow = prevRow,
                                    offsetX = prevX,
                                    offsetY = prevY,
                                )
                            } else {
                                val occupiedSlots = onScreenRemotes.mapNotNull { b ->
                                    if (b.id != remoteButton.id &&
                                        b.gridCol != null &&
                                        b.gridRow != null &&
                                        b.gridCol!! in 0 until hCount &&
                                        b.gridRow!! in 0 until vCount
                                    ) {
                                        b.gridCol!! to b.gridRow!!
                                    } else {
                                        null
                                    }
                                }.toSet() + reservedSlots

                                var fallbackSlot: Pair<Int, Int>? = null
                                for (r in 0 until vCount) {
                                    for (c in 0 until hCount) {
                                        if (!occupiedSlots.contains(c to r)) {
                                            fallbackSlot = c to r
                                            break
                                        }
                                    }
                                    if (fallbackSlot != null) break
                                }

                                if (fallbackSlot != null) {
                                    val (fc, fr) = fallbackSlot
                                    val fbX = gridSizeXPx * fc + (gridSizeXPx - buttonSize) / 2
                                    val fbY = gridSizeYPx * fr + (gridSizeYPx - buttonSize) / 2
                                    onScreenRemotes[index] = onScreenRemotes[index].copy(
                                        gridCol = fc,
                                        gridRow = fr,
                                        offsetX = fbX,
                                        offsetY = fbY,
                                    )
                                }
                            }
                        } else {
                            val otherIndex = onScreenRemotes.indexOfFirst { other ->
                                other.id != remoteButton.id &&
                                    other.gridCol == targetCol &&
                                    other.gridRow == targetRow
                            }

                            if (otherIndex != -1) {
                                if (prevWasValid) {
                                    val swappedOtherX =
                                        gridSizeXPx * prevCol + (gridSizeXPx - buttonSize) / 2
                                    val swappedOtherY =
                                        gridSizeYPx * prevRow + (gridSizeYPx - buttonSize) / 2
                                    onScreenRemotes[otherIndex] = onScreenRemotes[otherIndex].copy(
                                        gridCol = prevCol,
                                        gridRow = prevRow,
                                        offsetX = swappedOtherX,
                                        offsetY = swappedOtherY,
                                    )
                                } else {
                                    val occupiedSlots = onScreenRemotes.mapNotNull { b ->
                                        if (b.id != remoteButton.id &&
                                            b.id != onScreenRemotes[otherIndex].id &&
                                            b.gridCol != null &&
                                            b.gridRow != null
                                        ) {
                                            b.gridCol to b.gridRow
                                        } else {
                                            null
                                        }
                                    }.toSet() + setOf(targetCol to targetRow) + reservedSlots

                                    var emptySlot: Pair<Int, Int>? = null
                                    for (r in 0 until vCount) {
                                        for (c in 0 until hCount) {
                                            if (!occupiedSlots.contains(c to r)) {
                                                emptySlot = c to r
                                                break
                                            }
                                        }
                                        if (emptySlot != null) break
                                    }

                                    if (emptySlot != null) {
                                        val (ec, er) = emptySlot
                                        val otherX =
                                            gridSizeXPx * ec + (gridSizeXPx - buttonSize) / 2
                                        val otherY =
                                            gridSizeYPx * er + (gridSizeYPx - buttonSize) / 2
                                        onScreenRemotes[otherIndex] =
                                            onScreenRemotes[otherIndex].copy(
                                                gridCol = ec,
                                                gridRow = er,
                                                offsetX = otherX,
                                                offsetY = otherY,
                                            )
                                    } else {
                                        onScreenRemotes[otherIndex] =
                                            onScreenRemotes[otherIndex].copy(
                                                gridCol = prevCol,
                                                gridRow = prevRow,
                                            )
                                    }
                                }
                            }

                            onScreenRemotes[index] = onScreenRemotes[index].copy(
                                gridCol = targetCol,
                                gridRow = targetRow,
                                offsetX = snappedX,
                                offsetY = snappedY,
                            )
                        }
                    } else {
                        onScreenRemotes[index] = onScreenRemotes[index].copy(
                            offsetX = posX,
                            offsetY = posY,
                        )
                    }

                    if (remoteDataDBModel != null) {
                        onUpdate(
                            remoteDataDBModel.copy(
                                onScreenRemoteButtonDBS = onScreenRemotes.toList(),
                                offScreenRemoteButtonDBS = offScreenRemotes.toList(),
                            ),
                        )
                    }
                },
                onRemove = {
                    offScreenRemotes.add(onScreenRemotes.removeAt(index))
                    if (remoteDataDBModel != null) {
                        onUpdate(
                            remoteDataDBModel.copy(
                                onScreenRemoteButtonDBS = onScreenRemotes.toList(),
                                offScreenRemoteButtonDBS = offScreenRemotes.toList(),
                            ),
                        )
                    }
                },
            )
        }
    }
    val numberPosX = if (gridSizeXPx > 0) gridSizeXPx * 0 + (gridSizeXPx - buttonSize) / 2 else 0f
    val numberPosY =
        if (gridSizeYPx > 0) gridSizeYPx * (vCount - 1) + (gridSizeYPx - buttonSize) / 2 else 0f
    val extraPosX =
        if (gridSizeXPx > 0) gridSizeXPx * (hCount - 1) + (gridSizeXPx - buttonSize) / 2 else 0f
    val extraPosY =
        if (gridSizeYPx > 0) gridSizeYPx * (vCount - 1) + (gridSizeYPx - buttonSize) / 2 else 0f

    RemoteButtonDigits(
        digitButtons = remoteDataDBModel?.getAllDigits(),
        offsetX = with(LocalDensity.current) { numberPosX.toDp() },
        offsetY = with(LocalDensity.current) { numberPosY.toDp() },
        size = 72.dp,
        onClick = {},
    )

    RemoteButtonExtra(
        extraButtons = offScreenRemotes,
        offsetX = with(LocalDensity.current) { extraPosX.toDp() },
        offsetY = with(LocalDensity.current) { extraPosY.toDp() },
        size = 72.dp,
        onClick = { buttonToAdd ->
            val reservedSlots = setOf(0 to vCount - 1, hCount - 1 to vCount - 1)
            val occupiedSlots = onScreenRemotes.mapNotNull { b ->
                if (b.gridCol != null &&
                    b.gridRow != null &&
                    b.gridCol!! in 0 until hCount &&
                    b.gridRow!! in 0 until vCount
                ) {
                    b.gridCol!! to b.gridRow!!
                } else {
                    null
                }
            }.toSet() + reservedSlots

            // Find the first unoccupied slot from top-left
            var targetSlot: Pair<Int, Int>? = null
            for (r in 0 until vCount) {
                for (c in 0 until hCount) {
                    if (!occupiedSlots.contains(c to r)) {
                        targetSlot = c to r
                        break
                    }
                }
                if (targetSlot != null) break
            }

            val placedButton = if (targetSlot != null && gridSizeXPx > 0 && gridSizeYPx > 0) {
                val (c, r) = targetSlot
                buttonToAdd.copy(
                    gridCol = c,
                    gridRow = r,
                    offsetX = gridSizeXPx * c + (gridSizeXPx - buttonSize) / 2,
                    offsetY = gridSizeYPx * r + (gridSizeYPx - buttonSize) / 2,
                )
            } else {
                buttonToAdd
            }

            onScreenRemotes.add(placedButton)
            offScreenRemotes.remove(buttonToAdd)

            if (remoteDataDBModel != null) {
                onUpdate(
                    remoteDataDBModel.copy(
                        onScreenRemoteButtonDBS = onScreenRemotes.toList(),
                        offScreenRemoteButtonDBS = offScreenRemotes.toList(),
                    ),
                )
            }
        },
        onDelete = if (remoteDataDBModel?.isCustom == true) {
            { buttonToDelete ->
                offScreenRemotes.remove(buttonToDelete)
                onUpdate(
                    remoteDataDBModel.copy(
                        onScreenRemoteButtonDBS = onScreenRemotes.toList(),
                        offScreenRemoteButtonDBS = offScreenRemotes.toList(),
                    ),
                )
            }
        } else {
            null
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@Composable
fun UniversalRemote(
    remoteDataDBModel: RemoteDataDBModel?,
    layoutLimits: Rect,
    textOnlyMode: Boolean = false,
) {
    val context = LocalContext.current
    val irController = remember(context) {
        val manager = context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        if (manager != null) IRController(manager, context) else null
    }

    val vibrator = remember(context) {
        VibrationHelper.getVibrator(context)
    }

    val hCount = remoteDataDBModel?.gridHCount ?: 5
    val vCount = remoteDataDBModel?.gridVCount ?: 10
    val gridSizeXPx = if (layoutLimits.width > 0) layoutLimits.width / hCount.toFloat() else 0f
    val gridSizeYPx = if (layoutLimits.height > 0) layoutLimits.height / vCount.toFloat() else 0f
    val buttonSize = LocalDensity.current.run { 72.dp.toPx() }

    Box(modifier = Modifier.fillMaxSize()) {
        remoteDataDBModel?.onScreenRemoteButtonDBS?.forEach {
            val col = it.gridCol ?: if (gridSizeXPx > 0) {
                val cx = it.offsetX + buttonSize / 2f
                (cx / gridSizeXPx).toInt().coerceIn(0, hCount - 1)
            } else {
                0
            }
            val row = it.gridRow ?: if (gridSizeYPx > 0) {
                val cy = it.offsetY + buttonSize / 2f
                (cy / gridSizeYPx).toInt().coerceIn(0, vCount - 1)
            } else {
                0
            }

            val posX = if (gridSizeXPx > 0) {
                gridSizeXPx * col + (gridSizeXPx - buttonSize) / 2
            } else {
                it.offsetX
            }
            val posY = if (gridSizeYPx > 0) {
                gridSizeYPx * row + (gridSizeYPx - buttonSize) / 2
            } else {
                it.offsetY
            }

            RemoteButtonSingle(
                name = it.name,
                icon = it.getIcon(),
                textIcon = it.getTextIcon(),
                forceTextOnly = textOnlyMode,
                offsetX = posX,
                offsetY = posY,
                onClick = { it.transmit(irController, vibrator) },
            )
        }
        val numberPosX =
            if (gridSizeXPx > 0) gridSizeXPx * 0 + (gridSizeXPx - buttonSize) / 2 else 0f
        val numberPosY =
            if (gridSizeYPx > 0) gridSizeYPx * (vCount - 1) + (gridSizeYPx - buttonSize) / 2 else 0f
        val extraPosX =
            if (gridSizeXPx > 0) gridSizeXPx * (hCount - 1) + (gridSizeXPx - buttonSize) / 2 else 0f
        val extraPosY =
            if (gridSizeYPx > 0) gridSizeYPx * (vCount - 1) + (gridSizeYPx - buttonSize) / 2 else 0f

        RemoteButtonDigits(
            digitButtons = remoteDataDBModel?.getAllDigits(),
            offsetX = with(LocalDensity.current) { numberPosX.toDp() },
            offsetY = with(LocalDensity.current) { numberPosY.toDp() },
            size = 72.dp,
            onClick = { it.transmit(irController, vibrator) },
        )
        RemoteButtonExtra(
            extraButtons = remoteDataDBModel?.getAllOffScreen(),
            offsetX = with(LocalDensity.current) { extraPosX.toDp() },
            offsetY = with(LocalDensity.current) { extraPosY.toDp() },
            size = 72.dp,
            onClick = { it.transmit(irController, vibrator) },
        )
    }
}
