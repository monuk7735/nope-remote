package com.monuk7735.nope.remote.composables

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material.icons.outlined.BrightnessLow
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel

@Composable
fun LightRemote(
    remoteDataDBModel: RemoteDataDBModel?,
) {
    val irController =
        LocalContext.current.run {
            IRController(getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager)
        }

    val vibrator =
        LocalContext.current.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                getSystemService(Vibrator::class.java)!!
            }
        }

    val context = LocalContext.current

    fun transmit(name: String) {
        val button = remoteDataDBModel?.getByName(name)
        if (button != null) {
            button.transmit(irController, vibrator)
        } else {
            Toast.makeText(context, "Button $name not found", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Power Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConfigurableButton(
                onClick = { transmit("ON") },
                text = "ON",
                color = MaterialTheme.colorScheme.primaryContainer
            )
            
            IconButton(
                onClick = { transmit("POWER") },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PowerSettingsNew,
                    contentDescription = "Power",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(32.dp)
                )
            }

            ConfigurableButton(
                onClick = { transmit("OFF") },
                text = "OFF",
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }

        // Brightness & Flow
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ConfigurableIconButton(
                onClick = { transmit("BRIGHTNESS-") },
                icon = Icons.Outlined.BrightnessLow,
                contentDescription = "Brightness Down"
            )
            
            ConfigurableIconButton(
                onClick = { transmit("FLOW") },
                icon = Icons.Outlined.Waves,
                contentDescription = "Flow"
            )

            ConfigurableIconButton(
                onClick = { transmit("BRIGHTNESS+") },
                icon = Icons.Outlined.BrightnessHigh,
                contentDescription = "Brightness Up"
            )
        }

        // Colors Grid
        val colorButtons = listOf("COLOR0", "COLOR1", "COLOR2", "COLOR3", "COLOR4")
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Let the colors take available space but centered
                .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Colors",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                colorButtons.forEach { colorName ->
                    ConfigurableButton(
                        onClick = { transmit(colorName) },
                        text = colorName.replace("COLOR", "C"),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            }
        }

        // Navigation
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ConfigurableIconButton(
                onClick = { transmit("BACK") },
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back"
            )
            ConfigurableIconButton(
                onClick = { transmit("HOME") },
                icon = Icons.Outlined.Home,
                contentDescription = "Home"
            )
            ConfigurableIconButton(
                onClick = { transmit("MENU") },
                icon = Icons.Outlined.Menu,
                contentDescription = "Menu"
            )
        }
    }
}

@Composable
private fun ConfigurableButton(
    onClick: () -> Unit,
    text: String,
    color: Color
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ConfigurableIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
