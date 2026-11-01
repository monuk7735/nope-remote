package com.monuk7735.nope.remote.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.monuk7735.nope.remote.androidtv.proto.remote.Remotemessage
import com.monuk7735.nope.remote.models.database.AndroidTvButtonDBModel
import com.monuk7735.nope.remote.models.database.SavedTvDeviceDBModel

@Composable
fun AndroidTvGridSettingsDialog(
    hCount: Int,
    vCount: Int,
    onHCountChange: (Int) -> Unit,
    onVCountChange: (Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Grid Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Horizontal Cells: $hCount",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Slider(
                        value = hCount.toFloat(),
                        onValueChange = { onHCountChange(it.toInt()) },
                        valueRange = 2f..12f,
                        steps = 9,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Vertical Cells: $vCount",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Slider(
                        value = vCount.toFloat(),
                        onValueChange = { onVCountChange(it.toInt()) },
                        valueRange = 2f..20f,
                        steps = 17,
                    )
                }

                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun AndroidTvEditableRemote(
    tvDevice: SavedTvDeviceDBModel,
    onUpdate: (SavedTvDeviceDBModel) -> Unit,
    layoutLimits: Rect,
    hCount: Int = 5,
    vCount: Int = 10,
) {
    val gridSizeXPx = if (layoutLimits.width > 0) layoutLimits.width / hCount.toFloat() else 0f
    val gridSizeYPx = if (layoutLimits.height > 0) layoutLimits.height / vCount.toFloat() else 0f
    val buttonSize = LocalDensity.current.run { 72.dp.toPx() }

    val onScreenRemotes: MutableList<AndroidTvButtonDBModel> =
        remember(tvDevice.id) {
            val buttons = tvDevice.getEffectiveOnScreenButtons().map { btn ->
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
        }

    val offScreenRemotes: MutableList<AndroidTvButtonDBModel> =
        remember(tvDevice.id) {
            mutableStateListOf(elements = tvDevice.getEffectiveOffScreenButtons().toTypedArray())
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

                    onUpdate(
                        tvDevice.copy(
                            onScreenButtons = onScreenRemotes.toList(),
                            offScreenButtons = offScreenRemotes.toList(),
                        ),
                    )
                },
                onRemove = {
                    offScreenRemotes.add(onScreenRemotes.removeAt(index))
                    onUpdate(
                        tvDevice.copy(
                            onScreenButtons = onScreenRemotes.toList(),
                            offScreenButtons = offScreenRemotes.toList(),
                        ),
                    )
                },
            )
        }
    }

    AndroidTvButtonExtra(
        extraButtons = offScreenRemotes,
        offsetX = LocalDensity.current.run {
            (layoutLimits.right - layoutLimits.left).toDp() - 80.dp
        },
        offsetY = LocalDensity.current.run {
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

            onUpdate(
                tvDevice.copy(
                    onScreenButtons = onScreenRemotes.toList(),
                    offScreenButtons = offScreenRemotes.toList(),
                ),
            )
        },
    )
}

@Composable
fun AndroidTvUniversalRemote(
    tvDevice: SavedTvDeviceDBModel,
    layoutLimits: Rect,
    onSendKey: (Remotemessage.RemoteKeyCode) -> Unit,
    onOpenKeyboard: () -> Unit,
) {
    val hCount = tvDevice.gridHCount
    val vCount = tvDevice.gridVCount
    val gridSizeXPx = if (layoutLimits.width > 0) layoutLimits.width / hCount.toFloat() else 0f
    val gridSizeYPx = if (layoutLimits.height > 0) layoutLimits.height / vCount.toFloat() else 0f
    val buttonSize = LocalDensity.current.run { 72.dp.toPx() }

    Box(modifier = Modifier.fillMaxSize()) {
        tvDevice.getEffectiveOnScreenButtons().forEach { button ->
            val col = button.gridCol ?: if (gridSizeXPx > 0) {
                val cx = button.offsetX + buttonSize / 2f
                (cx / gridSizeXPx).toInt().coerceIn(0, hCount - 1)
            } else {
                0
            }
            val row = button.gridRow ?: if (gridSizeYPx > 0) {
                val cy = button.offsetY + buttonSize / 2f
                (cy / gridSizeYPx).toInt().coerceIn(0, vCount - 1)
            } else {
                0
            }

            val posX = if (gridSizeXPx > 0) {
                gridSizeXPx * col + (gridSizeXPx - buttonSize) / 2
            } else {
                button.offsetX
            }
            val posY = if (gridSizeYPx > 0) {
                gridSizeYPx * row + (gridSizeYPx - buttonSize) / 2
            } else {
                button.offsetY
            }

            val isPower = button.keyCodeName.contains("POWER")
            val contentColor = if (isPower) {
                MaterialTheme.colorScheme.error
            } else {
                null
            }

            RemoteButtonSingle(
                name = button.name,
                icon = button.getIcon(),
                textIcon = button.getTextIcon(),
                offsetX = posX,
                offsetY = posY,
                contentColor = contentColor,
                onClick = {
                    if (button.isKeyboardAction()) {
                        onOpenKeyboard()
                    } else {
                        button.getRemoteKeyCode()?.let { onSendKey(it) }
                    }
                },
            )
        }

        val offScreenNonDigits = tvDevice.getAllOffScreenNonDigits()
        if (offScreenNonDigits.isNotEmpty()) {
            AndroidTvButtonExtra(
                extraButtons = offScreenNonDigits,
                offsetX = LocalDensity.current.run {
                    (layoutLimits.right - layoutLimits.left).toDp() - 80.dp
                },
                offsetY = LocalDensity.current.run {
                    (layoutLimits.bottom - layoutLimits.top).toDp() - 80.dp
                },
                size = 80.dp,
                onClick = { button ->
                    if (button.isKeyboardAction()) {
                        onOpenKeyboard()
                    } else {
                        button.getRemoteKeyCode()?.let { onSendKey(it) }
                    }
                },
            )
        }

        val digits = tvDevice.getAllDigits()
        if (digits.isNotEmpty()) {
            AndroidTvButtonDigits(
                digitButtons = digits,
                offsetX = 0.dp,
                offsetY = LocalDensity.current.run {
                    (layoutLimits.bottom - layoutLimits.top).toDp() - 80.dp
                },
                size = 80.dp,
                onClick = { button ->
                    button.getRemoteKeyCode()?.let { onSendKey(it) }
                },
            )
        }
    }
}
