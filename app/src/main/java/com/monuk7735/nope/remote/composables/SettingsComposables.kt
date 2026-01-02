package com.monuk7735.nope.remote.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
                        fontWeight = FontWeight.SemiBold
                )
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor =
                                                MaterialTheme.colorScheme.surfaceContainerLow
                                )
                ) { Column(modifier = Modifier.padding(vertical = 8.dp)) { content() } }
        }
}

@Composable
fun SwitchPreference(
        modifier: Modifier = Modifier,
        title: String,
        summary: String?,
        icon: ImageVector? = null,
        value: Boolean,
        onValueChange: (newValue: Boolean) -> Unit,
) {
        Row(
                modifier =
                        modifier
                                .clickable { onValueChange(!value) }
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
                if (icon != null) {
                        Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                }
                Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                        Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                        )
                        if (summary != null) {
                                Text(
                                        text = summary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                }
                Switch(
                        checked = value,
                        onCheckedChange = { onValueChange(it) }
                )
        }
}

@Composable
fun InfoPreference(
        title: String,
        value: String,
        icon: ImageVector? = null
) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
                if (icon != null) {
                        Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                }
                Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                        Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                        )
                        Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }
        }
}

@Composable
fun SingleChoicePreference(
        title: String,
        summary: String?,
        icon: ImageVector? = null,
        options: List<String>,
        selectedOption: Int,
        onOptionSelected: (Int) -> Unit
) {
        var showDialog by
                remember { mutableStateOf(false) }

        if (showDialog) {
                AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(text = title) },
                        text = {
                                Column {
                                        options.forEachIndexed { index, option ->
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .clickable {
                                                                                onOptionSelected(
                                                                                        index
                                                                                )
                                                                                showDialog = false
                                                                        }
                                                                        .padding(vertical = 12.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        RadioButton(
                                                                selected =
                                                                        (index == selectedOption),
                                                                onClick = null
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                                text = option,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyLarge
                                                        )
                                                }
                                        }
                                }
                        },
                        confirmButton = {
                                TextButton(
                                        onClick = { showDialog = false }
                                ) { Text("Cancel") }
                        },
                        shape = RoundedCornerShape(28.dp)
                )
        }

        Row(
                modifier =
                        Modifier.clickable { showDialog = true }
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
                if (icon != null) {
                        Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                }
                Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                        Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                        )
                        if (summary != null) {
                                Text(
                                        text = summary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                }
        }
}

@Composable
fun RepositoryPreference(
        name: String,
        url: String,
        isInstalled: Boolean,
        state: com.monuk7735.nope.remote.service.RepoState,
        onDownload: () -> Unit,
        onDelete: () -> Unit,
        onUrlClick: () -> Unit
) {
        val isRunning = state.state == com.monuk7735.nope.remote.service.DownloadState.DOWNLOADING || 
                        state.state == com.monuk7735.nope.remote.service.DownloadState.EXTRACTING
        val isError = state.state == com.monuk7735.nope.remote.service.DownloadState.ERROR

        Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
                if (isRunning) {
                     CircularProgressIndicator(
                         modifier = Modifier.size(24.dp),
                         strokeWidth = 3.dp
                     )
                } else if (isInstalled) {
                     Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Installed",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                     )
                } else if (isError) {
                     Icon(
                        imageVector = Icons.Outlined.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                     )
                } else {
                     Icon(
                        imageVector = Icons.Outlined.CloudDownload,
                        contentDescription = "Download",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                     )
                }

                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                        Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onUrlClick() }
                        )
                        
                        if (isError || isRunning) {
                                Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                                )
                        }

                        if (isRunning) {
                                Spacer(modifier = Modifier.height(8.dp))
                                if (state.isIndeterminate) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    LinearProgressIndicator(
                                        progress = { state.progress },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                        }
                }

                if (!isRunning) {
                        if (isInstalled) {
                                IconButton(onClick = onDelete) {
                                        Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error
                                        )
                                }
                        } else {
                                Button(onClick = onDownload) {
                                        Text(if (isError) "Retry" else "Download")
                                }
                        }
                }
        }
}

@Composable
fun LogOutputView(output: String) {
        if (output.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Command Output",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(reverseLayout = true) {
                    item {
                        Text(
                            text = output,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
