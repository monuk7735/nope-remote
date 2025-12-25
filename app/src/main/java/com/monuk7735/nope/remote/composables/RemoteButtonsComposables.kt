package com.monuk7735.nope.remote.composables

import android.view.MotionEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import com.monuk7735.nope.remote.R
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel
import com.monuk7735.nope.remote.ui.theme.icons.Digit_0

@ExperimentalComposeUiApi
@Composable
fun RemoteButtonSingleEditable(
    name: String,
    icon: ImageVector,
    offsetX: Float,
    offsetY: Float,
    layoutLimits: Rect,
    onPosUpdate: (offsetX: Float, offsetY: Float) -> Unit,
    onRemove: () -> Unit,
) {

    var localOffSetX by remember {
        mutableStateOf(offsetX)
    }
    var localOffSetY by remember {
        mutableStateOf(offsetY)
    }

    var prevX by remember {
        mutableStateOf(0f)
    }
    var prevY by remember {
        mutableStateOf(0f)
    }

    val size = 80.dp
    val sizeInFloat = LocalDensity.current.run { size.toPx() }

    ConstraintLayout(
        modifier = Modifier
            .offset(
                x = with(LocalDensity.current) {
                    localOffSetX.toDp()
                },
                y = with(LocalDensity.current) {
                    localOffSetY.toDp()
                }
            )
            .wrapContentSize()
            .border(5.dp, MaterialTheme.colorScheme.primary),
    ) {
        val (mainIcon, delIcon) = createRefs()
        Icon(
            modifier = Modifier
                .pointerInteropFilter { event ->
                    when (event.action) {
                        MotionEvent.ACTION_UP -> {
                            onPosUpdate(
                                localOffSetX,
                                localOffSetY
                            )
                        }
                        MotionEvent.ACTION_DOWN -> {
                            prevX = event.rawX
                            prevY = event.rawY
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val tempOffsetX = localOffSetX + event.rawX - prevX

                            if (tempOffsetX > 0f && tempOffsetX < layoutLimits.right - sizeInFloat - layoutLimits.left)
                                localOffSetX += event.rawX - prevX

                            val tempOffsetY = localOffSetY + event.rawY - prevY
                            if (tempOffsetY > 0f && tempOffsetY < layoutLimits.bottom - sizeInFloat - layoutLimits.top)
                                localOffSetY += event.rawY - prevY

                            prevX = event.rawX
                            prevY = event.rawY
                        }
                    }
                    return@pointerInteropFilter true
                }
                .size(size)
                .padding(5.dp)
                .clip(RoundedCornerShape(50.dp))
                .constrainAs(mainIcon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .background(
                    MaterialTheme.colorScheme.primaryContainer
                )
                .padding(22.dp),
            imageVector = icon,
            contentDescription = name,
        )
        Icon(
            modifier = Modifier
                .clickable { onRemove() }
                .padding(5.dp)
                .size(20.dp)
                .constrainAs(delIcon) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                },
            painter = painterResource(id = R.drawable.ic_remove),
            contentDescription = "Delete $name",
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun RemoteButtonSingle(
    name: String,
    icon: ImageVector,
    offsetX: Float,
    offsetY: Float,
    onClick: () -> Unit,
) {

    val size = 80.dp

    Box(
        modifier = Modifier
            .offset(
                x = with(LocalDensity.current) {
                    offsetX.toDp()
                },
                y = with(LocalDensity.current) {
                    offsetY.toDp()
                }
            )
            .size(size)
            .padding(5.dp)
            .clip(RoundedCornerShape(50.dp))
            .clickable { onClick() }
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = name,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@ExperimentalFoundationApi
@Composable
fun RemoteButtonExtra(
    extraButtons: List<RemoteButtonDBModel>?,
    offsetX: Dp,
    offsetY: Dp,
    size: Dp,
    onClick: (remoteButtonDBModel: RemoteButtonDBModel) -> Unit,
) {
    var dialogVisible by remember {
        mutableStateOf(false)
    }

    if (extraButtons?.size ?: 0 > 0)
        Box(
            modifier = Modifier
                .offset(
                    x = offsetX,
                    y = offsetY
                )
                .size(size)
                .padding(5.dp)
                .clip(RoundedCornerShape(50.dp))
                .clickable(extraButtons?.isNotEmpty() ?: false) {
                    dialogVisible = true
                }
                .background(
                    MaterialTheme.colorScheme.primaryContainer
                )
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "Extra Buttons",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    if (dialogVisible)
        Dialog(
            onDismissRequest = { dialogVisible = false }
        ) {
            Card(
                modifier = Modifier.padding(10.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    content = {
                        if (extraButtons == null) {
                            return@LazyVerticalGrid
                        }

                        items(extraButtons.size) { index ->
                            RemoteButtonOverflow(
                                remoteButtonModel = extraButtons[index],
                                onClick = {
                                    onClick(extraButtons[index])
                                }
                            )
                        }
                    }
                )
            }
        }
}

@Composable
fun RemoteButtonOverflow(
    modifier: Modifier = Modifier,
    remoteButtonModel: RemoteButtonDBModel,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clickable { onClick() }
            .padding(10.dp)
            .padding(
                top = 5.dp,
                bottom = 5.dp,
                start = 20.dp,
                end = 20.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = remoteButtonModel.name,
            textAlign = TextAlign.Center
        )
    }
}

@ExperimentalFoundationApi
@Composable
fun RemoteButtonDigits(
    digitButtons: List<RemoteButtonDBModel>?,
    offsetX: Dp,
    offsetY: Dp,
    size: Dp,
    onClick: (remoteButtonDBModel: RemoteButtonDBModel) -> Unit,
) {
    var dialogVisible by remember {
        mutableStateOf(false)
    }
    if (digitButtons?.size ?: 0 > 0)
        Box(
            modifier = Modifier
                .offset(
                    x = offsetX,
                    y = offsetY
                )
                .size(size)
                .padding(5.dp)
                .clip(RoundedCornerShape(50.dp))
                .clickable(digitButtons?.isNotEmpty() ?: false) {
                    dialogVisible = true
                }
                .background(
                    MaterialTheme.colorScheme.primaryContainer
                )
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Digit_0,
                contentDescription = "Extra Buttons",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    if (dialogVisible)
        Dialog(
            onDismissRequest = { dialogVisible = false }
        ) {
            Card(
                modifier = Modifier.padding(1.dp)
            ) {
                LazyVerticalGrid(
                    modifier = Modifier.padding(5.dp),
                    columns = GridCells.Fixed(3),
                    content = {
                        if (digitButtons == null) {
                            return@LazyVerticalGrid
                        }
                        items(digitButtons.size - 1) { index ->
                            RemoteButtonSingle(
                                name = digitButtons[index + 1].name,
                                icon = digitButtons[index + 1].getIcon(),
                                offsetX = 0f,
                                offsetY = 0f,
                                onClick = {
                                    onClick(digitButtons[index + 1])
                                }
                            )
                        }
                        item { }
                        item {
                            RemoteButtonSingle(
                                name = digitButtons[0].name,
                                icon = digitButtons[0].getIcon(),
                                offsetX = 0f,
                                offsetY = 0f,
                                onClick = {
                                    onClick(digitButtons[0])
                                }
                            )
                        }
                    }
                )
            }
        }
}
