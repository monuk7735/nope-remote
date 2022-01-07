package com.monuk7735.nope.remote.ui.theme.icons

import androidx.compose.material.Icon
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

internal val SetTopBox: ImageVector
    get() {
        if (setTopBox != null) {
            return setTopBox!!
        }
        setTopBox = materialIcon(name = "SetTopBox") {
            materialPath {

                moveTo(2f,10f)
                horizontalLineToRelative(20f)
                verticalLineToRelative(5f)
                horizontalLineToRelative(-20f)
                verticalLineToRelative(-5f)
                horizontalLineToRelative(1f)
                verticalLineToRelative(4f)
                horizontalLineToRelative(18f)
                verticalLineToRelative(-3f)
                horizontalLineToRelative(-18f)
                close()

                moveTo(5f, 13f)


            }
        }
        return setTopBox!!
    }

private var setTopBox: ImageVector? = null

@Preview(showBackground = true)
@Composable
fun setTopBox(){
    Icon(imageVector = SetTopBox, contentDescription = "")
}
