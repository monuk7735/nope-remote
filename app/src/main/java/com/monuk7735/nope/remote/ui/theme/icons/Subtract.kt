package com.monuk7735.nope.remote.ui.theme.icons

import androidx.compose.material.Icon
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

val Subtract: ImageVector
    get() {
//        if (subtract != null) {
//            return subtract!!
//        }
        subtract = materialIcon(name = "Subtract") {
            materialPath {
                moveTo(5f,11f)
                horizontalLineToRelative(14f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(-14f)
                close()
            }
        }
        return subtract!!
    }

private var subtract: ImageVector? = null

@Preview
@Composable
fun asdfsdf() {
    Icon(Subtract, "")
}
