package com.monuk7735.nope.remote.composables.utils

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex

@Composable
fun rememberReorderableState(listState: LazyListState, onMove: (Int, Int) -> Unit): ReorderableState {
    return remember { ReorderableState(listState, onMove) }
}

class ReorderableState(
    val listState: LazyListState,
    val onMove: (Int, Int) -> Unit
) {
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
                val currentOffset = (draggingItemIndex?.let { listState.layoutInfo.visibleItemsInfo.find { i -> i.index == it }?.offset } ?: 0) + dragOffset
                
                // Simple collision detection
                 currentOffset.toInt() in itemStart..itemEnd
            }?.index

        if (hoveredIndex != null && hoveredIndex != draggingItemIndex && draggingItemIndex != null) {
            onMove(draggingItemIndex!!, hoveredIndex)
            draggingItemIndex = hoveredIndex
            dragOffset = 0f // Reset offset after swap implies new position
        }
    }

    fun onDragStop() {
        draggingItemIndex = null
        dragOffset = 0f
    }
}

fun Modifier.draggedItem(
    reorderableState: ReorderableState,
    index: Int
): Modifier = this.then(
    Modifier
        .zIndex(if (reorderableState.draggingItemIndex == index) 1f else 0f)
        .graphicsLayer {
            translationY = if (reorderableState.draggingItemIndex == index) reorderableState.draggedItemOffset else 0f
        }
)

fun Modifier.detectReorder(reorderableState: ReorderableState, index: Int): Modifier {
    return this.pointerInput(Unit) {
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
            }
        )
    }
}
