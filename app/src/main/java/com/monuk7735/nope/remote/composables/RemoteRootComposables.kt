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
            val isInvalid = isOutOfBounds || isColliding

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

                        // Check if another button occupies targetCol, targetRow
                        val otherIndex = onScreenRemotes.indexOfFirst { other ->
                            other.id != remoteButton.id &&
                                other.gridCol == targetCol &&
                                other.gridRow == targetRow
                        }

                        val prevCol = remoteButton.gridCol
                        val prevRow = remoteButton.gridRow
                        val prevWasValid = prevCol != null &&
                            prevRow != null &&
                            prevCol in 0 until hCount &&
                            prevRow in 0 until vCount

                        if (otherIndex != -1) {
                            if (prevWasValid) {
                                // Swap positions with occupying button
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
                                // Dragged button was invalid:
                                // find first unoccupied cell for the other button
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
                                }.toSet() + setOf(targetCol to targetRow)

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
        onClick = { buttonToAdd ->
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
            }.toSet()

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
    RemoteButtonDigits(
        digitButtons = remoteDataDBModel?.getAllDigits(),
        offsetX = 0.dp,
        offsetY =
        LocalDensity.current.run {
            (layoutLimits.bottom - layoutLimits.top).toDp() - 80.dp
        },
        size = 80.dp,
        onClick = {},
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
            onClick = { it.transmit(irController, vibrator) },
        )
        RemoteButtonDigits(
            digitButtons = remoteDataDBModel?.getAllDigits(),
            offsetX = 0.dp,
            offsetY =
            LocalDensity.current.run {
                (layoutLimits.bottom - layoutLimits.top).toDp() - 80.dp
            },
            size = 80.dp,
            onClick = { it.transmit(irController, vibrator) },
        )
    }
}
