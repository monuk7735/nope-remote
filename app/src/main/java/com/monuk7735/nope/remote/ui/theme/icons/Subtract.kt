package com.monuk7735.nope.remote.ui.theme.icons

import androidx.compose.material3.Icon
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

val Subtract: ImageVector
    get() {
        if (_subtract != null) {
            return _subtract!!
        }
        _subtract = materialIcon(name = "Subtract") {
            materialPath {
                moveTo(5f,11f)
                horizontalLineToRelative(14f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(-14f)
                close()
            }
        }
        return _subtract!!
    }

private var _subtract: ImageVector? = null

@Preview
@Composable
fun SubtractPreview() {
    Icon(Subtract, "")
}
