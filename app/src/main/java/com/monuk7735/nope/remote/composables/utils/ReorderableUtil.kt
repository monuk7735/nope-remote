package com.monuk7735.nope.remote.composables.utils

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex

@Composable
fun rememberReorderableState(
    listState: LazyListState,
    onMove: (Int, Int) -> Unit,
): ReorderableState = remember { ReorderableState(listState, onMove) }

class ReorderableState(val listState: LazyListState, val onMove: (Int, Int) -> Unit) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    var dragOffset by mutableStateOf(0f)
        private set

    internal val draggedItemOffset get() = dragOffset

    fun onDragStart(index: Int) {
        draggingItemIndex = index
    }

    fun onDrag(offset: Float) {
        dragOffset += offset

        val hoveredIndex = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { item ->
                val itemStart = item.offset
                val itemEnd = item.offset + item.size
                val draggedItem = draggingItemIndex?.let { idx ->
                    listState.layoutInfo.visibleItemsInfo.find { i -> i.index == idx }
                }
                val currentOffset = (draggedItem?.offset ?: 0) + dragOffset

                currentOffset.toInt() in itemStart..itemEnd
            }?.index

        if (hoveredIndex != null &&
            hoveredIndex != draggingItemIndex &&
            draggingItemIndex != null
        ) {
            onMove(draggingItemIndex!!, hoveredIndex)
            draggingItemIndex = hoveredIndex
            dragOffset = 0f
        }
    }

    fun onDragStop() {
        draggingItemIndex = null
        dragOffset = 0f
    }
}

fun Modifier.draggedItem(reorderableState: ReorderableState, index: Int): Modifier = this.then(
    Modifier
        .zIndex(if (reorderableState.draggingItemIndex == index) 1f else 0f)
        .graphicsLayer {
            translationY =
                if (reorderableState.draggingItemIndex ==
                    index
                ) {
                    reorderableState.draggedItemOffset
                } else {
                    0f
                }
        },
)

fun Modifier.detectReorder(reorderableState: ReorderableState, index: Int): Modifier =
    this.pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
            onDragStart = {
                reorderableState.onDragStart(index)
            },
            onDrag = { _, dragAmount ->
                reorderableState.onDrag(dragAmount.y)
            },
            onDragEnd = {
                reorderableState.onDragStop()
            },
            onDragCancel = {
                reorderableState.onDragStop()
            },
        )
    }
