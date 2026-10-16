package com.monuk7735.nope.remote.composables

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@ExperimentalMaterial3Api
@Composable
fun AppBar(
    title: String = "Title",
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = title,
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            }
        },
        actions = actions,
    )
}

@Composable
fun ActionButton(name: String, icon: ImageVector, onClick: () -> Unit) {
    Icon(
        imageVector = icon,
        contentDescription = name,
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                onClick()
            }
            .padding(10.dp)
            .size(25.dp),
    )
}

@Composable
fun ActionButton(name: String, icon: Painter, onClick: () -> Unit) {
    Icon(
        painter = icon,
        contentDescription = name,
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                onClick()
            }
            .padding(10.dp)
            .size(25.dp),
        tint = MaterialTheme.colorScheme.onPrimary,
    )
}

@Composable
fun OverFlowActionButton(
    name: String,
    icon: ImageVector,
    onClick: (intent: Intent) -> Unit,
    overFlowItems: Map<String, Intent>,
) {
    var visibile by remember {
        mutableStateOf(false)
    }
    Icon(
        imageVector = icon,
        contentDescription = name,
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                visibile = true
            }
            .padding(10.dp)
            .size(25.dp),

    )
    DropdownMenu(
        expanded = visibile,
        onDismissRequest = {
            visibile = false
        },
        offset = DpOffset(0.dp, (-90).dp),
    ) {
        overFlowItems.forEach {
            DropdownMenuItem(
                text = {
                    Text(text = it.key)
                },
                onClick = {
                    visibile = false
                    onClick(it.value)
                },
            )
        }
    }
}

@Composable
fun LoadingDialog(visible: Boolean) {
    if (visible) {
        Dialog(onDismissRequest = {
        }) {
            Card {
                Row(
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 30.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "Loading",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
