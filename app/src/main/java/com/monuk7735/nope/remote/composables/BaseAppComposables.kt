package com.monuk7735.nope.remote.composables

import android.content.Intent
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.defaultDecayAnimationSpec
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
    actions: @Composable RowScope.() -> Unit = {},
) {
    SmallTopAppBar(
        title = {
            Text(
                text = title,
//                fontWeight = FontWeight.Bold
            )
        },
        actions = actions,
    )
}

@Composable
fun ActionButton(
    name: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Icon(
        imageVector = icon,
        contentDescription = name,
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                onClick()
            }
            .padding(10.dp)
            .size(25.dp)
    )
}

@Composable
fun ActionButton(
    name: String,
    icon: Painter,
    onClick: () -> Unit,
) {
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
        tint = MaterialTheme.colorScheme.onPrimary
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
            .size(25.dp)

    )
    DropdownMenu(
        expanded = visibile,
        onDismissRequest = {
            visibile = false
        },
        offset = DpOffset(0.dp, (-90).dp)
    ) {
        overFlowItems.forEach {
            DropdownMenuItem(
                onClick = {
                    visibile = false
                    onClick(it.value)
                }
            ) {
                Text(text = it.key)
            }
        }
    }
}

@Composable
fun LoadingDialog(
    visible: Boolean,
) {
    if (visible)
        Dialog(onDismissRequest = {

        }) {
            Card {
                Row(
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "Loading",
                        fontWeight = FontWeight.Bold)
                }
            }
        }
}