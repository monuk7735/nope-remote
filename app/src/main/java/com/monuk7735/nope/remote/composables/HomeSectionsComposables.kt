package com.monuk7735.nope.remote.composables

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.monuk7735.nope.remote.R
import com.monuk7735.nope.remote.composables.utils.detectImmediateReorder
import com.monuk7735.nope.remote.composables.utils.detectReorder
import com.monuk7735.nope.remote.composables.utils.draggedItem
import com.monuk7735.nope.remote.composables.utils.rememberReorderableState
import com.monuk7735.nope.remote.utils.HomeSection
import com.monuk7735.nope.remote.utils.HomeSectionItem
import com.monuk7735.nope.remote.utils.HomeSectionsManager

@Composable
fun ReorderHomeSectionsDialog(
    initialSections: List<HomeSectionItem>,
    onSave: (List<HomeSectionItem>) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var sections by remember(initialSections) { mutableStateOf(initialSections) }

    val listState = rememberLazyListState()
    val reorderableState = rememberReorderableState(
        listState = listState,
        onMove = { from, to ->
            if (from in sections.indices && to in sections.indices) {
                val updated = sections.toMutableList()
                updated.add(to, updated.removeAt(from))
                sections = updated
                onSave(updated)
            }
        },
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = {
            Text(
                text = "Customize Sections",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Drag the handle to reorder sections. Use the switches to show or " +
                        "hide them. At least one section must stay enabled.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    state = reorderableState.listState,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(sections.size, key = { sections[it].section.id }) { index ->
                        val item = sections[index]
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .draggedItem(reorderableState, index),
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                ),
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // Drag handle
                                    Icon(
                                        imageVector = Icons.Outlined.DragHandle,
                                        contentDescription = "Drag to reorder",
                                        modifier = Modifier
                                            .size(28.dp)
                                            .detectImmediateReorder(reorderableState, index)
                                            .detectReorder(reorderableState, index),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Section Icon
                                    if (item.section == HomeSection.WIFI) {
                                        Icon(
                                            imageVector = Icons.Outlined.Tv,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_ir_remote),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Title
                                    Text(
                                        text = item.section.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                    )

                                    // Arrow button to move up/down easily
                                    if (sections.size > 1) {
                                        IconButton(
                                            onClick = {
                                                val target = if (index == 0) 1 else 0
                                                val updated = sections.toMutableList()
                                                updated.add(target, updated.removeAt(index))
                                                sections = updated
                                                onSave(updated)
                                            },
                                            modifier = Modifier.size(32.dp),
                                        ) {
                                            Icon(
                                                imageVector = if (index == 0) {
                                                    Icons.Outlined.KeyboardArrowDown
                                                } else {
                                                    Icons.Outlined.KeyboardArrowUp
                                                },
                                                contentDescription =
                                                if (index == 0) "Move down" else "Move up",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }

                                    // Toggle switch
                                    Switch(
                                        checked = item.isEnabled,
                                        onCheckedChange = { checked ->
                                            if (HomeSectionsManager.canToggleSection(
                                                    sections,
                                                    item.section,
                                                    checked,
                                                )
                                            ) {
                                                val updated = sections.map {
                                                    if (it.section == item.section) {
                                                        it.copy(isEnabled = checked)
                                                    } else {
                                                        it
                                                    }
                                                }
                                                sections = updated
                                                onSave(updated)
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "At least one section must be enabled",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    val defaults = HomeSectionsManager.resetToDefault(context)
                    sections = defaults
                    onSave(defaults)
                },
            ) {
                Text("Reset")
            }
        },
    )
}
