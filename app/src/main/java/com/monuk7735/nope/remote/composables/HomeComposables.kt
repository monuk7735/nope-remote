package com.monuk7735.nope.remote.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyState(
        modifier: Modifier = Modifier,
        text: String,
        secondaryText: String? = null,
        icon: ImageVector
) {
    Column(
            modifier = modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
                modifier = Modifier.size(160.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Outer glow
                Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ) {}

                Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
        )
        if (secondaryText != null) {
            Text(
                    text = secondaryText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp).widthIn(max = 280.dp),
                    lineHeight = 22.sp
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RemoteTile(
        modifier: Modifier = Modifier,
        name: String,
        deviceType: String? = null,
        icon: ImageVector,
        onClick: () -> Unit,
        onLongClick: (() -> Unit)? = null,
) {
    Card(
            modifier =
                    modifier.clip(RoundedCornerShape(32.dp))
                            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle background accent
            Box(
                    modifier =
                            Modifier.align(Alignment.TopEnd)
                                    .size(80.dp)
                                    .background(
                                            androidx.compose.ui.graphics.Brush.radialGradient(
                                                    colors =
                                                            listOf(
                                                                    MaterialTheme.colorScheme
                                                                            .primary.copy(
                                                                            alpha = 0.05f
                                                                    ),
                                                                    Color.Transparent
                                                            )
                                            )
                                    )
            )

            Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Column {
                    Text(
                            text = name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                    )
                    if (deviceType != null) {
                        Text(
                                text = deviceType.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MacroSequenceCard(
        modifier: Modifier = Modifier,
        name: String,
        stepCount: Int,
        onExecute: () -> Unit,
        onEdit: () -> Unit,
) {
    Card(
            modifier =
                    modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .clickable { onExecute() },
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                                imageVector = Icons.Outlined.AutoFixHigh,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                                imageVector = Icons.Outlined.Layers,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                                text = "$stepCount Commands",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    onClick = onEdit
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit $name",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// @Preview(showBackground = true)
// @Composable
// fun MacroPreview() {
//    Column(modifier = Modifier.fillMaxWidth(1f)) {
//        MacroParent(
//            modifier = Modifier,
//            name = "Xiaomi TV",
//            onClick = {
//
//            }
//        )
//    }
// }

//
// @Preview(showBackground = true)
// @Composable
// fun Preview() {
//    Column(modifier = Modifier.fillMaxWidth(1f)) {
//        RemoteParent(
//            modifier = Modifier,
//            name = "Xiaomi TV",
//            icon = Icons.Filled.Send,
//            onClick = {
//
//            }
//        )
//    }
// }
