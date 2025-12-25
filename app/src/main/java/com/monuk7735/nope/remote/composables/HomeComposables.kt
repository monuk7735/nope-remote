package com.monuk7735.nope.remote.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Decorative icon
            modifier = Modifier.size(120.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.LightGray
        )
    }
}

@Composable
fun RemoteParent(
    modifier: Modifier = Modifier,
    name: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        shape = RoundedCornerShape(5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    onClick()
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Text(
                text = name
            )
        }
    }
}

@Composable
fun MacroParent(
    modifier: Modifier = Modifier,
    name: String,
    onExecute: () -> Unit,
    onEdit: () -> Unit,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        shape = RoundedCornerShape(5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    onExecute()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier
                    .padding(
                        start = 20.dp
                    ),
                text = name
            )
            Icon(
                modifier = Modifier
                    .clickable {
                        onEdit()
                    }
                    .size(80.dp)
                    .padding(25.dp),
                imageVector = Icons.Outlined.KeyboardArrowRight,
                contentDescription = "Edit $name"
            )

        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun FlowPreview() {
//    Column(modifier = Modifier.fillMaxWidth(1f)) {
//        FlowParent(
//            modifier = Modifier,
//            name = "Xiaomi TV",
//            onClick = {
//
//            }
//        )
//    }
//}

//
//@Preview(showBackground = true)
//@Composable
//fun Preview() {
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
//}
