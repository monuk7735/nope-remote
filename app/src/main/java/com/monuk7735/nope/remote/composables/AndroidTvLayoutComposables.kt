package com.monuk7735.nope.remote.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PowerSettingsNew
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

    val numberPosX = if (gridSizeXPx > 0) gridSizeXPx * 0 + (gridSizeXPx - buttonSize) / 2 else 0f
    val numberPosY =
        if (gridSizeYPx > 0) gridSizeYPx * (vCount - 1) + (gridSizeYPx - buttonSize) / 2 else 0f
    val extraPosX =
        if (gridSizeXPx > 0) gridSizeXPx * (hCount - 1) + (gridSizeXPx - buttonSize) / 2 else 0f
    val extraPosY =
        if (gridSizeYPx > 0) gridSizeYPx * (vCount - 1) + (gridSizeYPx - buttonSize) / 2 else 0f

    val digits = tvDevice.getAllDigits()
    if (digits.isNotEmpty()) {
        AndroidTvButtonDigits(
            digitButtons = digits,
            offsetX = with(LocalDensity.current) { numberPosX.toDp() },
            offsetY = with(LocalDensity.current) { numberPosY.toDp() },
            size = 72.dp,
            onClick = { /* Preview numbers in editor */ },
        )
    }

    AndroidTvButtonExtra(
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
fun AndroidTvDefaultRemote(
    tvDevice: SavedTvDeviceDBModel,
    onSendKey: (Remotemessage.RemoteKeyCode) -> Unit,
    onOpenKeyboard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val offScreenNonDigits = tvDevice.getAllOffScreenNonDigits()
    val digits = tvDevice.getAllDigits()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // 1. Top Section: Power in top center
        RemoteButtonSingle(
            name = "Power",
            icon = Icons.Outlined.PowerSettingsNew,
            offsetX = 0f,
            offsetY = 0f,
            contentColor = MaterialTheme.colorScheme.error,
            onClick = { onSendKey(Remotemessage.RemoteKeyCode.KEYCODE_POWER) },
        )

        // 2. Navigation Section: Joined Modern D-Pad
        RemoteDPadCluster(
            onUp = { onSendKey(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_UP) },
            onDown = { onSendKey(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_DOWN) },
            onLeft = { onSendKey(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_LEFT) },
            onRight = { onSendKey(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_RIGHT) },
            onCenter = { onSendKey(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_CENTER) },
        )

        // 3. System Section: Joined System Row (Back | Home | Menu)
        RemoteSystemButtonsRow(
            onBack = { onSendKey(Remotemessage.RemoteKeyCode.KEYCODE_BACK) },
            onHome = { onSendKey(Remotemessage.RemoteKeyCode.KEYCODE_HOME) },
            onMenu = { onSendKey(Remotemessage.RemoteKeyCode.KEYCODE_MENU) },
            modifier = Modifier.fillMaxWidth(0.92f),
        )

        // 4. Bottom Controls: Numbers on left, Joined Volume in center, Extras on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (digits.isNotEmpty()) {
                AndroidTvButtonDigits(
                    digitButtons = digits,
                    size = 64.dp,
                    onClick = { button ->
                        button.getRemoteKeyCode()?.let { onSendKey(it) }
                    },
                )
            } else {
                Spacer(modifier = Modifier.size(64.dp))
            }

            RemoteVolumePill(
                onVolumeUp = { onSendKey(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_UP) },
                onVolumeDown = { onSendKey(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_DOWN) },
            )

            if (offScreenNonDigits.isNotEmpty()) {
                AndroidTvButtonExtra(
                    extraButtons = offScreenNonDigits,
                    size = 64.dp,
                    onClick = { button ->
                        if (button.isKeyboardAction()) {
                            onOpenKeyboard()
                        } else {
                            button.getRemoteKeyCode()?.let { onSendKey(it) }
                        }
                    },
                )
            } else {
                Spacer(modifier = Modifier.size(64.dp))
            }
        }
    }
}

@Composable
fun AndroidTvUniversalRemote(
    tvDevice: SavedTvDeviceDBModel,
    layoutLimits: Rect,
    onSendKey: (Remotemessage.RemoteKeyCode) -> Unit,
    onOpenKeyboard: () -> Unit,
) {
    if (tvDevice.onScreenButtons == null) {
        AndroidTvDefaultRemote(
            tvDevice = tvDevice,
            onSendKey = onSendKey,
            onOpenKeyboard = onOpenKeyboard,
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            val hCount = tvDevice.gridHCount
            val vCount = tvDevice.gridVCount
            val padPx = LocalDensity.current.run { 32.dp.toPx() }
            val effectiveWidth = if (layoutLimits.width >
                padPx
            ) {
                layoutLimits.width - padPx
            } else {
                layoutLimits.width
            }
            val effectiveHeight = if (layoutLimits.height >
                padPx
            ) {
                layoutLimits.height - padPx
            } else {
                layoutLimits.height
            }
            val gridSizeXPx = if (effectiveWidth > 0) effectiveWidth / hCount.toFloat() else 0f
            val gridSizeYPx = if (effectiveHeight > 0) effectiveHeight / vCount.toFloat() else 0f
            val buttonSize = LocalDensity.current.run { 72.dp.toPx() }

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

            val numberPosX =
                if (gridSizeXPx > 0) gridSizeXPx * 0 + (gridSizeXPx - buttonSize) / 2 else 0f
            val numberPosY =
                if (gridSizeYPx >
                    0
                ) {
                    gridSizeYPx * (vCount - 1) + (gridSizeYPx - buttonSize) / 2
                } else {
                    0f
                }
            val extraPosX =
                if (gridSizeXPx >
                    0
                ) {
                    gridSizeXPx * (hCount - 1) + (gridSizeXPx - buttonSize) / 2
                } else {
                    0f
                }
            val extraPosY =
                if (gridSizeYPx >
                    0
                ) {
                    gridSizeYPx * (vCount - 1) + (gridSizeYPx - buttonSize) / 2
                } else {
                    0f
                }

            val digits = tvDevice.getAllDigits()
            if (digits.isNotEmpty()) {
                AndroidTvButtonDigits(
                    digitButtons = digits,
                    offsetX = with(LocalDensity.current) { numberPosX.toDp() },
                    offsetY = with(LocalDensity.current) { numberPosY.toDp() },
                    size = 72.dp,
                    onClick = { button ->
                        button.getRemoteKeyCode()?.let { onSendKey(it) }
                    },
                )
            }

            val offScreenNonDigits = tvDevice.getAllOffScreenNonDigits()
            if (offScreenNonDigits.isNotEmpty()) {
                AndroidTvButtonExtra(
                    extraButtons = offScreenNonDigits,
                    offsetX = with(LocalDensity.current) { extraPosX.toDp() },
                    offsetY = with(LocalDensity.current) { extraPosY.toDp() },
                    size = 72.dp,
                    onClick = { button ->
                        if (button.isKeyboardAction()) {
                            onOpenKeyboard()
                        } else {
                            button.getRemoteKeyCode()?.let { onSendKey(it) }
                        }
                    },
                )
            }
        }
    }
}
