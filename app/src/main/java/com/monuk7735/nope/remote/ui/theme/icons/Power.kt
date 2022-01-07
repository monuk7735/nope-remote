package com.monuk7735.nope.remote.ui.theme.icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

internal val Power: ImageVector
    get() {
        if (power != null) {
            return power!!
        }
        power = materialIcon(name = "Power") {
            materialPath {

                moveTo(13f, 3f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(10f)
                horizontalLineToRelative(2f)
                lineTo(13f, 3f)
                close()
                moveTo(17.83f, 5.17f)
                lineToRelative(-1.42f, 1.42f)
                curveTo(17.99f, 7.86f, 19f, 9.81f, 19f, 12f)
                curveToRelative(0f, 3.87f, -3.13f, 7f, -7f, 7f)
                reflectiveCurveToRelative(-7f, -3.13f, -7f, -7f)
                curveToRelative(0f, -2.19f, 1.01f, -4.14f, 2.58f, -5.42f)
                lineTo(6.17f, 5.17f)
                curveTo(4.23f, 6.82f, 3f, 9.26f, 3f, 12f)
                curveToRelative(0f, 4.97f, 4.03f, 9f, 9f, 9f)
                reflectiveCurveToRelative(9f, -4.03f, 9f, -9f)
                curveToRelative(0f, -2.74f, -1.23f, -5.18f, -3.17f, -6.83f)
                close()
            }
        }
        return power!!
    }

private var power: ImageVector? = null
