package com.monuk7735.nope.remote.composables

import android.view.MotionEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel
import com.monuk7735.nope.remote.ui.theme.icons.*

@ExperimentalComposeUiApi
@Composable
fun RemoteButtonSingleEditable(
        name: String,
        icon: ImageVector?,
        textIcon: String? = null,
        offsetX: Float,
        offsetY: Float,
        layoutLimits: Rect,
        onPosUpdate: (offsetX: Float, offsetY: Float) -> Unit,
        onRemove: () -> Unit,
) {
    var localOffSetX by remember { mutableStateOf(offsetX) }
    var localOffSetY by remember { mutableStateOf(offsetY) }
    var prevX by remember { mutableStateOf(0f) }
    var prevY by remember { mutableStateOf(0f) }

    // Sync with parent state when it changes (e.g. initial load or external update)
    LaunchedEffect(offsetX, offsetY) {
        localOffSetX = offsetX
        localOffSetY = offsetY
    }

    val size = 72.dp
    val sizeInFloat = LocalDensity.current.run { size.toPx() }

    Box(
            modifier =
                    Modifier.offset(
                                    x = with(LocalDensity.current) { localOffSetX.toDp() },
                                    y = with(LocalDensity.current) { localOffSetY.toDp() }
                            )
                            .size(size)
                            .padding(4.dp)
                            .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(20.dp)
                            ),
            contentAlignment = Alignment.Center
    ) {
        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .background(
                                        Brush.radialGradient(
                                                colors =
                                                        listOf(
                                                                MaterialTheme.colorScheme.error
                                                                        .copy(alpha = 0.05f),
                                                                Color.Transparent
                                                        )
                                        )
                                )
                                .pointerInteropFilter { event ->
                                    when (event.action) {
                                        MotionEvent.ACTION_UP ->
                                                onPosUpdate(localOffSetX, localOffSetY)
                                        MotionEvent.ACTION_DOWN -> {
                                            prevX = event.rawX
                                            prevY = event.rawY
                                        }
                                        MotionEvent.ACTION_MOVE -> {
                                            val dx = event.rawX - prevX
                                            val dy = event.rawY - prevY

                                            val nextX = localOffSetX + dx
                                            if (nextX > 0f &&
                                                            nextX <
                                                                    layoutLimits.right -
                                                                            sizeInFloat -
                                                                            layoutLimits.left
                                            )
                                                    localOffSetX = nextX

                                            val nextY = localOffSetY + dy
                                            if (nextY > 0f &&
                                                            nextY <
                                                                    layoutLimits.bottom -
                                                                            sizeInFloat -
                                                                            layoutLimits.top
                                            )
                                                    localOffSetY = nextY

                                            prevX = event.rawX
                                            prevY = event.rawY
                                        }
                                    }
                                    return@pointerInteropFilter true
                                }
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (icon != null) {
                    Icon(
                            imageVector = icon,
                            contentDescription = name,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                            text = textIcon ?: name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }

        Surface(
                modifier =
                        Modifier.align(Alignment.TopStart)
                                .offset(x = (-4).dp, y = (-4).dp)
                                .size(24.dp)
                                .clickable { onRemove() },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error,
                shadowElevation = 4.dp
        ) {
            Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.padding(6.dp),
                    tint = MaterialTheme.colorScheme.onError
            )
        }
    }
}

@Composable
fun RemoteButtonSingle(
        name: String,
        icon: ImageVector?,
        textIcon: String? = null,
        offsetX: Float,
        offsetY: Float,
        onClick: () -> Unit,
) {
    val size = 72.dp

    Surface(
            modifier =
                    Modifier.offset(
                                    x = with(LocalDensity.current) { offsetX.toDp() },
                                    y = with(LocalDensity.current) { offsetY.toDp() }
                            )
                            .size(size)
                            .padding(4.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            onClick = onClick
    ) {
        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .background(
                                        Brush.radialGradient(
                                                colors =
                                                        listOf(
                                                                MaterialTheme.colorScheme.primary
                                                                        .copy(alpha = 0.05f),
                                                                Color.Transparent
                                                        )
                                        )
                                ),
                contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                        imageVector = icon,
                        contentDescription = name,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                        text = textIcon ?: name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
fun RemoteButtonExtra(
        extraButtons: List<RemoteButtonDBModel>?,
        offsetX: Dp,
        offsetY: Dp,
        size: Dp,
        onClick: (remoteButtonDBModel: RemoteButtonDBModel) -> Unit,
) {
    var dialogVisible by remember { mutableStateOf(false) }

    if (extraButtons?.isNotEmpty() == true) {
        Surface(
                modifier = Modifier.offset(x = offsetX, y = offsetY).size(72.dp).padding(4.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                tonalElevation = 4.dp,
                onClick = { dialogVisible = true }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Extras",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }

    if (dialogVisible) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
                onDismissRequest = { dialogVisible = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .padding(bottom = 32.dp) // Space for navigation bar
            ) {
                Text(
                        text = "Extra Buttons",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.primary
                )
                LazyVerticalGrid(
                        columns = GridCells.Adaptive(100.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .heightIn(max = 400.dp)
                ) {
                    extraButtons?.let { buttons ->
                        items(buttons.size) { index ->
                            RemoteButtonOverflow(
                                    remoteButtonModel = buttons[index],
                                    onClick = {
                                        onClick(buttons[index])
                                        dialogVisible = false
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RemoteButtonOverflow(
        modifier: Modifier = Modifier,
        remoteButtonModel: RemoteButtonDBModel,
        onClick: () -> Unit,
) {
    Card(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
            modifier = modifier.fillMaxWidth()
    ) {
        Box(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                contentAlignment = Alignment.Center
        ) {
            Text(
                    text = remoteButtonModel.name,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
            )
        }
    }
}

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
fun RemoteButtonDigits(
        digitButtons: List<RemoteButtonDBModel>?,
        offsetX: Dp,
        offsetY: Dp,
        size: Dp,
        onClick: (remoteButtonDBModel: RemoteButtonDBModel) -> Unit,
) {
    var dialogVisible by remember { mutableStateOf(false) }

    if (digitButtons?.isNotEmpty() == true) {
        Surface(
                modifier = Modifier.offset(x = offsetX, y = offsetY).size(72.dp).padding(4.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                tonalElevation = 4.dp,
                onClick = { dialogVisible = true }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                        imageVector = Icons.Outlined.Numbers,
                        contentDescription = "Digits",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }

    if (dialogVisible) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
                onDismissRequest = { dialogVisible = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                        text = "Numbers",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.primary
                )
                LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.widthIn(max = 300.dp).padding(bottom = 16.dp)
                ) {
                    digitButtons?.let { buttons ->
                        // 1-9
                        items(buttons.size - 1) { index ->
                            RemoteButtonSingle(
                                    name = buttons[index + 1].name,
                                    icon = buttons[index + 1].getIcon(),
                                    textIcon = buttons[index + 1].getTextIcon(),
                                    offsetX = 0f,
                                    offsetY = 0f,
                                    onClick = { onClick(buttons[index + 1]) }
                            )
                        }
                        item { /* Empty for alignment */}
                        // 0
                        item {
                            RemoteButtonSingle(
                                    name = buttons[0].name,
                                    icon = buttons[0].getIcon(),
                                    textIcon = buttons[0].getTextIcon(),
                                    offsetX = 0f,
                                    offsetY = 0f,
                                    onClick = { onClick(buttons[0]) }
                            )
                        }
                    }
                }
            }
        }
    }
}
