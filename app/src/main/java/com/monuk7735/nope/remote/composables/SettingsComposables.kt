package com.monuk7735.nope.remote.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SwitchPreference(
    modifier: Modifier = Modifier,
    title: String,
    summary: String?,
    value: Boolean,
    onValueChange: (newValue: Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .clickable {
                onValueChange(!value)
            }
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = title
            )
            Text(
                fontSize = 12.sp,
                text = summary ?: ""
            )
        }
        Switch(
            checked = value,
            onCheckedChange = {
                onValueChange(it)
            }
        )
    }
}