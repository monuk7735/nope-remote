package com.monuk7735.nope.remote.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.monuk7735.nope.remote.backup.ConflictStrategy
import com.monuk7735.nope.remote.backup.ExportOptions
import com.monuk7735.nope.remote.backup.ImportOptions
import com.monuk7735.nope.remote.backup.NopeBackup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExportBackupDialog(
    irCount: Int,
    wifiCount: Int,
    macroCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (ExportOptions) -> Unit,
) {
    var includeIr by remember { mutableStateOf(irCount > 0) }
    var includeWifi by remember { mutableStateOf(wifiCount > 0) }
    var includeMacros by remember { mutableStateOf(macroCount > 0) }
    var includeLayouts by remember { mutableStateOf(true) }

    val hasAnySelected = includeIr || includeWifi || includeMacros

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Backup Data",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Select what you want to export to a backup file:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(4.dp))

                CheckboxOptionRow(
                    title = "Infrared Remotes",
                    subtitle = "$irCount remotes available",
                    checked = includeIr,
                    enabled = irCount > 0,
                    onCheckedChange = { includeIr = it },
                )

                CheckboxOptionRow(
                    title = "Android TV Remotes",
                    subtitle = "$wifiCount remotes available",
                    checked = includeWifi,
                    enabled = wifiCount > 0,
                    onCheckedChange = { includeWifi = it },
                )

                CheckboxOptionRow(
                    title = "Macros",
                    subtitle = "$macroCount macros available",
                    checked = includeMacros,
                    enabled = macroCount > 0,
                    onCheckedChange = { includeMacros = it },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                CheckboxOptionRow(
                    title = "Remote Layouts",
                    subtitle = "Include custom button positions and grid dimensions",
                    checked = includeLayouts,
                    enabled = true,
                    onCheckedChange = { includeLayouts = it },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        ExportOptions(
                            includeIrRemotes = includeIr,
                            includeWifiRemotes = includeWifi,
                            includeMacros = includeMacros,
                            includeLayouts = includeLayouts,
                        ),
                    )
                },
                enabled = hasAnySelected,
            ) {
                Text("Export")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
fun ImportBackupDialog(
    backup: NopeBackup,
    onDismiss: () -> Unit,
    onConfirm: (ImportOptions) -> Unit,
) {
    val irCount = backup.irRemotes?.size ?: 0
    val wifiCount = backup.wifiRemotes?.size ?: 0
    val macroCount = backup.macros?.size ?: 0
    val hasLayouts = backup.includesLayouts

    var restoreIr by remember { mutableStateOf(irCount > 0) }
    var restoreWifi by remember { mutableStateOf(wifiCount > 0) }
    var restoreMacros by remember { mutableStateOf(macroCount > 0) }
    var restoreLayouts by remember { mutableStateOf(hasLayouts) }
    var conflictStrategy by remember { mutableStateOf(ConflictStrategy.OVERWRITE) }

    val hasAnySelected = restoreIr || restoreWifi || restoreMacros
    val formattedDate = remember(backup.exportDate) {
        val formatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        formatter.format(Date(backup.exportDate))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Restore Data",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Exported: $formattedDate",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "App v${backup.appVersion} • Backup Format v${backup.version}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (backup.version > NopeBackup.CURRENT_BACKUP_VERSION) {
                            Text(
                                text = "Notice: Backup created with a newer app version.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Select what to restore:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )

                CheckboxOptionRow(
                    title = "Infrared Remotes",
                    subtitle = "$irCount remotes in backup",
                    checked = restoreIr,
                    enabled = irCount > 0,
                    onCheckedChange = { restoreIr = it },
                )

                CheckboxOptionRow(
                    title = "Android TV Remotes",
                    subtitle = "$wifiCount remotes in backup",
                    checked = restoreWifi,
                    enabled = wifiCount > 0,
                    onCheckedChange = { restoreWifi = it },
                )

                CheckboxOptionRow(
                    title = "Macros",
                    subtitle = "$macroCount macros in backup",
                    checked = restoreMacros,
                    enabled = macroCount > 0,
                    onCheckedChange = { restoreMacros = it },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                CheckboxOptionRow(
                    title = "Remote Layouts",
                    subtitle = if (hasLayouts) {
                        "Restore custom button positions and grid sizes"
                    } else {
                        "No custom layout data in backup"
                    },
                    checked = restoreLayouts,
                    enabled = hasLayouts,
                    onCheckedChange = { restoreLayouts = it },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "If a remote already exists:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )

                RadioOptionRow(
                    title = "Overwrite existing",
                    selected = conflictStrategy == ConflictStrategy.OVERWRITE,
                    onClick = { conflictStrategy = ConflictStrategy.OVERWRITE },
                )

                RadioOptionRow(
                    title = "Keep both (create copy)",
                    selected = conflictStrategy == ConflictStrategy.KEEP_BOTH,
                    onClick = { conflictStrategy = ConflictStrategy.KEEP_BOTH },
                )

                RadioOptionRow(
                    title = "Skip existing",
                    selected = conflictStrategy == ConflictStrategy.SKIP,
                    onClick = { conflictStrategy = ConflictStrategy.SKIP },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        ImportOptions(
                            restoreIrRemotes = restoreIr,
                            restoreWifiRemotes = restoreWifi,
                            restoreMacros = restoreMacros,
                            restoreLayouts = restoreLayouts,
                            conflictStrategy = conflictStrategy,
                        ),
                    )
                },
                enabled = hasAnySelected,
            ) {
                Text("Restore")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun CheckboxOptionRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    },
                )
            }
        }
    }
}

@Composable
private fun RadioOptionRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
