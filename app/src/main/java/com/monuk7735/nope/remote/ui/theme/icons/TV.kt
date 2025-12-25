package com.monuk7735.nope.remote.ui.theme.icons

import androidx.compose.material3.Icon
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

val TV: ImageVector
    get() {
        if (_tv != null) {
            return _tv!!
        }
        _tv = materialIcon(name = "TV") {
            materialPath {
                moveTo(4f,5f)
                horizontalLineToRelative(16f)
                verticalLineToRelative(12f)
                horizontalLineToRelative(-16f)
                verticalLineToRelative(-12f)
                horizontalLineToRelative(1f)
                verticalLineToRelative(11f)
                horizontalLineToRelative(14f)
                verticalLineToRelative(-10f)
                horizontalLineToRelative(-15f)
                close()

                moveTo(9f, 17f)
                horizontalLineToRelative(7f)
                verticalLineToRelative(1f)
                horizontalLineToRelative(-7f)
                verticalLineToRelative(-1f)
                close()
            }
        }
        return _tv!!
    }

private var _tv: ImageVector? = null

@Preview(showBackground = true)
@Composable
fun TvPreview(){
    Icon(imageVector = TV, contentDescription = "")
}
