package com.monuk7735.nope.remote.composables

import android.os.Vibrator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.infrared.patterns.Denon
import com.monuk7735.nope.remote.infrared.patterns.IRPattern
import com.monuk7735.nope.remote.infrared.patterns.IRPatternDecoder
import com.monuk7735.nope.remote.infrared.patterns.IRPatternType
import com.monuk7735.nope.remote.infrared.patterns.JVC
import com.monuk7735.nope.remote.infrared.patterns.Mitsubishi
import com.monuk7735.nope.remote.infrared.patterns.NEC48k
import com.monuk7735.nope.remote.infrared.patterns.NECExtended
import com.monuk7735.nope.remote.infrared.patterns.NECSamsung
import com.monuk7735.nope.remote.infrared.patterns.NECStandard
import com.monuk7735.nope.remote.infrared.patterns.Nokia32
import com.monuk7735.nope.remote.infrared.patterns.Panasonic
import com.monuk7735.nope.remote.infrared.patterns.Protocol
import com.monuk7735.nope.remote.infrared.patterns.RC5
import com.monuk7735.nope.remote.infrared.patterns.RC6Mode0
import com.monuk7735.nope.remote.infrared.patterns.Samsung20
import com.monuk7735.nope.remote.infrared.patterns.Samsung36
import com.monuk7735.nope.remote.infrared.patterns.SonySIRC
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel

enum class ProtocolOption(
    val displayName: String,
    val defaultFrequency: Int,
    val description: String,
) {
    NEC("NEC Standard", 38028, "Most common TV, soundbar & media protocol"),
    NEC_EXTENDED("NEC Extended", 38028, "NEC with 16-bit address (Apple, Aiwa)"),
    NEC_48K("NEC 48kHz", 48000, "High frequency NEC variant"),
    NEC_SAMSUNG("Samsung (NECx2)", 38028, "Standard modern Samsung TVs & audio"),
    SAMSUNG_20("Samsung 20-bit", 38400, "Legacy Samsung equipment"),
    SAMSUNG_36("Samsung 36-bit", 38000, "Samsung climate systems & recent displays"),
    SONY_12("Sony SIRC 12-bit", 40000, "Sony TVs & monitors"),
    SONY_15("Sony SIRC 15-bit", 40000, "Sony DVD, VCR & audio devices"),
    SONY_20("Sony SIRC 20-bit", 40000, "Advanced Sony home theater receivers"),
    RC5("Philips RC-5", 36000, "Philips, Marantz & European audio gear"),
    RC6_MODE0("Philips RC-6 (Mode 0)", 36000, "Philips & Media Center decoders"),
    PANASONIC("Panasonic / Kaseikyo", 36700, "Panasonic TVs, DVD & ACs"),
    JVC("JVC", 38000, "JVC audio & television"),
    DENON("Denon", 38000, "Denon & Sharp AV equipment"),
    MITSUBISHI("Mitsubishi", 38000, "Mitsubishi TVs & projectors"),
    NOKIA_32("Nokia 32-bit", 38000, "Nokia & European satellite boxes"),
}

private val commonKeyPresets = listOf(
    "POWER", "VOL+", "VOL-", "MUTE", "CH+", "CH-",
    "OK", "UP", "DOWN", "LEFT", "RIGHT",
    "BACK", "HOME", "MENU", "EXIT", "INPUT",
    "PLAY", "PAUSE", "STOP", "REW", "FWD",
    "RED", "GREEN", "YELLOW", "BLUE",
    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
)

private fun parseNumberInput(input: String, isHex: Boolean): Int? {
    val clean = input.trim()
    if (clean.isEmpty()) return null
    return try {
        if (clean.startsWith("0x", ignoreCase = true)) {
            clean.substring(2).toInt(16)
        } else if (isHex) {
            clean.toInt(16)
        } else {
            clean.toInt(10)
        }
    } catch (_: Exception) {
        null
    }
}

fun generateProtocolPattern(
    protocol: ProtocolOption,
    device: Int,
    subdevice: Int,
    function: Int,
): IRPattern {
    val (generator: Protocol, freq: Int) = when (protocol) {
        ProtocolOption.NEC -> Pair(NECStandard(), 38028)
        ProtocolOption.NEC_EXTENDED -> Pair(NECExtended(), 38028)
        ProtocolOption.NEC_48K -> Pair(NEC48k(), 48000)
        ProtocolOption.NEC_SAMSUNG -> Pair(NECSamsung(), 38028)
        ProtocolOption.SAMSUNG_20 -> Pair(Samsung20(), 38400)
        ProtocolOption.SAMSUNG_36 -> Pair(Samsung36(), 38000)
        ProtocolOption.SONY_12 -> Pair(SonySIRC(12), 40000)
        ProtocolOption.SONY_15 -> Pair(SonySIRC(15), 40000)
        ProtocolOption.SONY_20 -> Pair(SonySIRC(20), 40000)
        ProtocolOption.RC5 -> Pair(RC5(), 36000)
        ProtocolOption.RC6_MODE0 -> Pair(RC6Mode0(), 36000)
        ProtocolOption.PANASONIC -> Pair(Panasonic(), 36700)
        ProtocolOption.JVC -> Pair(JVC(), 38000)
        ProtocolOption.DENON -> Pair(Denon(), 38000)
        ProtocolOption.MITSUBISHI -> Pair(Mitsubishi(), 38000)
        ProtocolOption.NOKIA_32 -> Pair(Nokia32(), 38000)
    }

    val timings = generator.generate(device, subdevice, function)
    if (timings.isEmpty()) throw IllegalStateException("Generated timings are empty")
    return IRPattern(IRPatternType.Intervals, freq, timings.toIntArray())
}

fun generateFrequencyPattern(frequencyStr: String, dataStr: String): IRPattern {
    val trimmedData = dataStr.trim()
    if (trimmedData.isEmpty()) throw IllegalArgumentException("Interval data cannot be empty")

    // Check if Pronto Hex
    if (trimmedData.startsWith("0000 ", ignoreCase = true) ||
        (trimmedData.startsWith("0000") && trimmedData.length > 20)
    ) {
        return IRPatternDecoder(trimmedData).irPattern
    }

    val frequency = frequencyStr.trim().toIntOrNull() ?: 38000
    val tokens = trimmedData.split("[,\\s]+".toRegex()).filter { it.isNotBlank() }
    val timings = tokens.mapNotNull { it.toIntOrNull() }

    if (timings.isEmpty()) {
        throw IllegalArgumentException("No valid integer pulse durations found")
    }

    return IRPattern(IRPatternType.Intervals, frequency, timings.toIntArray())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomKeyDialog(
    onDismissRequest: () -> Unit,
    onAddKey: (name: String, pattern: IRPattern) -> Unit,
    irController: IRController?,
    vibrator: Vibrator,
) {
    var keyName by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Protocol, 1: Frequency

    // Protocol inputs
    var selectedProtocol by remember { mutableStateOf(ProtocolOption.NEC) }
    var isHexFormat by remember { mutableStateOf(true) }
    var deviceInput by remember { mutableStateOf("0") }
    var subdeviceInput by remember { mutableStateOf("0") }
    var functionInput by remember { mutableStateOf("") }
    var protocolDropdownExpanded by remember { mutableStateOf(false) }

    // Frequency inputs
    var carrierFreqInput by remember { mutableStateOf("38000") }
    var intervalsInput by remember { mutableStateOf("") }

    // Status feedback
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isStatusError by remember { mutableStateOf(false) }

    fun buildCurrentPattern(): Result<IRPattern> {
        return try {
            val pattern = if (selectedTab == 0) {
                val dev = parseNumberInput(deviceInput, isHexFormat)
                    ?: return Result.failure(
                        IllegalArgumentException("Invalid Device / Address number"),
                    )
                val sub = parseNumberInput(subdeviceInput, isHexFormat) ?: 0
                val func = parseNumberInput(functionInput, isHexFormat)
                    ?: return Result.failure(
                        IllegalArgumentException("Function / Command code is required"),
                    )
                generateProtocolPattern(selectedProtocol, dev, sub, func)
            } else {
                generateFrequencyPattern(carrierFreqInput, intervalsInput)
            }
            Result.success(pattern)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            ) {
                // Top header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Add Remote Button",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Configure code & label for custom key",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Button Label Section with Preview
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                OutlinedTextField(
                                    value = keyName,
                                    onValueChange = {
                                        keyName = it
                                        statusMessage = null
                                    },
                                    label = { Text("Button Name / Label *") },
                                    placeholder = { Text("e.g., POWER, VOL+, MUTE") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                // Button Preview Tile
                                val dummyButton = remember(keyName) {
                                    RemoteButtonDBModel(
                                        offsetX = 0f,
                                        offsetY = 0f,
                                        name = keyName.ifBlank { "?" },
                                        irPattern = IRPattern(
                                            IRPatternType.Intervals,
                                            38000,
                                            intArrayOf(100, 100),
                                        ),
                                    )
                                }
                                Surface(
                                    modifier = Modifier.size(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    ),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        val icon = dummyButton.getIcon()
                                        val textIcon = dummyButton.getTextIcon()
                                        if (icon != null) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp),
                                            )
                                        } else {
                                            Text(
                                                text = textIcon ?: if (keyName.isBlank()) {
                                                    "?"
                                                } else {
                                                    keyName.take(3)
                                                },
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick preset chips
                            Text(
                                text = "Quick Presets:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                commonKeyPresets.forEach { preset ->
                                    val isSelected = keyName.equals(preset, ignoreCase = true)
                                    SuggestionChip(
                                        onClick = { keyName = preset },
                                        label = {
                                            Text(
                                                preset,
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                        },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = if (isSelected) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            },
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    // Mode Selection Tabs
                    PrimaryTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Protocol Based", fontWeight = FontWeight.SemiBold) },
                            icon = {
                                Icon(
                                    Icons.Outlined.AccountTree,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Frequency Based", fontWeight = FontWeight.SemiBold) },
                            icon = {
                                Icon(
                                    Icons.Outlined.Sensors,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                        )
                    }

                    // TAB 0: Protocol Based Configuration
                    if (selectedTab == 0) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            ),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                // Protocol Selector Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = protocolDropdownExpanded,
                                    onExpandedChange = { protocolDropdownExpanded = it },
                                ) {
                                    OutlinedTextField(
                                        value = selectedProtocol.displayName,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Protocol") },
                                        supportingText = { Text(selectedProtocol.description) },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = protocolDropdownExpanded,
                                            )
                                        },
                                        modifier = Modifier
                                            .menuAnchor(
                                                ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                            )
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                    ExposedDropdownMenu(
                                        expanded = protocolDropdownExpanded,
                                        onDismissRequest = { protocolDropdownExpanded = false },
                                    ) {
                                        ProtocolOption.values().forEach { option ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(
                                                            option.displayName,
                                                            fontWeight = FontWeight.SemiBold,
                                                        )
                                                        Text(
                                                            "${option.defaultFrequency / 1000} " +
                                                                "kHz • ${option.description}",
                                                            style = MaterialTheme
                                                                .typography
                                                                .bodySmall,
                                                            color = MaterialTheme
                                                                .colorScheme
                                                                .onSurfaceVariant,
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    selectedProtocol = option
                                                    protocolDropdownExpanded = false
                                                },
                                            )
                                        }
                                    }
                                }

                                // Hex vs Decimal Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Input Format:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = isHexFormat,
                                            onClick = { isHexFormat = true },
                                            label = { Text("Hex (0x..)") },
                                        )
                                        FilterChip(
                                            selected = !isHexFormat,
                                            onClick = { isHexFormat = false },
                                            label = { Text("Decimal") },
                                        )
                                    }
                                }

                                // Device, Subdevice, Function Fields
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    OutlinedTextField(
                                        value = deviceInput,
                                        onValueChange = { deviceInput = it },
                                        label = { Text("Device") },
                                        placeholder = { Text(if (isHexFormat) "0x00" else "0") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                    OutlinedTextField(
                                        value = subdeviceInput,
                                        onValueChange = { subdeviceInput = it },
                                        label = { Text("Subdevice") },
                                        placeholder = { Text(if (isHexFormat) "0x00" else "0") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                }

                                OutlinedTextField(
                                    value = functionInput,
                                    onValueChange = { functionInput = it },
                                    label = { Text("Function / Command Code *") },
                                    placeholder = {
                                        Text(if (isHexFormat) "e.g., 0x12 or 12" else "e.g., 18")
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                )
                            }
                        }
                    } else {
                        // TAB 1: Frequency Based Configuration
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            ),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                OutlinedTextField(
                                    value = carrierFreqInput,
                                    onValueChange = { carrierFreqInput = it },
                                    label = { Text("Carrier Frequency (Hz)") },
                                    placeholder = { Text("38000") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                )

                                // Frequency shortcut chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    listOf("36000", "38000", "40000", "56000").forEach { freq ->
                                        SuggestionChip(
                                            onClick = { carrierFreqInput = freq },
                                            label = { Text("${freq.toInt() / 1000} kHz") },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = if (carrierFreqInput == freq) {
                                                    MaterialTheme.colorScheme.primaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.surface
                                                },
                                            ),
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = intervalsInput,
                                    onValueChange = { intervalsInput = it },
                                    label = { Text("Pattern / Intervals (Microseconds) *") },
                                    placeholder = { Text("9000, 4500, 560, 560, 560, 1690...") },
                                    minLines = 4,
                                    maxLines = 6,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    supportingText = {
                                        Text(
                                            "Standard Android ConsumerIR pattern " +
                                                "(alternating mark/space in µs). " +
                                                "Pronto Hex (0000 ...) is also supported.",
                                        )
                                    },
                                )
                            }
                        }
                    }

                    // Status Message Display
                    if (statusMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isStatusError) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = if (isStatusError) {
                                        Icons.Outlined.ErrorOutline
                                    } else {
                                        Icons.Outlined.CheckCircle
                                    },
                                    contentDescription = null,
                                    tint = if (isStatusError) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = statusMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isStatusError) {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Test IR Button
                    OutlinedButton(
                        onClick = {
                            val result = buildCurrentPattern()
                            result.onSuccess { pattern ->
                                irController?.transmit(pattern, vibrator)
                                statusMessage = "Signal transmitted (" +
                                    "${pattern.frequency} Hz, " +
                                    "${pattern.intervals.size} pulses)!"
                                isStatusError = false
                            }.onFailure { err ->
                                statusMessage = err.message ?: "Failed to generate signal"
                                isStatusError = true
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(
                            Icons.Outlined.FlashOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Signal")
                    }

                    // Add Button Action
                    Button(
                        onClick = {
                            if (keyName.isBlank()) {
                                statusMessage = "Please enter a name for the button"
                                isStatusError = true
                                return@Button
                            }
                            val result = buildCurrentPattern()
                            result.onSuccess { pattern ->
                                onAddKey(keyName.trim(), pattern)
                                onDismissRequest()
                            }.onFailure { err ->
                                statusMessage = err.message ?: "Failed to generate IR code"
                                isStatusError = true
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Button")
                    }
                }
            }
        }
    }
}
