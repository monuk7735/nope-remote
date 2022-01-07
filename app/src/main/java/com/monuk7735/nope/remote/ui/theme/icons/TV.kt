package com.monuk7735.nope.remote.ui.theme.icons

import androidx.compose.material.Icon
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

internal val TV: ImageVector
    get() {
        if (tv != null) {
            return tv!!
        }
        tv = materialIcon(name = "TV") {
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
        return tv!!
    }

private var tv: ImageVector? = null

@Preview(showBackground = true)
@Composable
fun tv(){
    Icon(imageVector = TV, contentDescription = "")
}
