package com.monuk7735.nope.remote.ui.theme.icons

import androidx.compose.material3.Icon
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.monuk7735.nope.remote.ui.theme.NopeRemoteTheme

val Digit_0: ImageVector
    get() {
        if (digit0 != null) {
            return digit0!!
        }
        digit0 = materialIcon(name = "Digit 0") {
            materialPath {
                moveTo(14.82f, 7.05f)
                curveToRelative(-0.34f, -0.4f, -0.75f, -0.7f, -1.23f, -0.88f)
                curveToRelative(-0.47f, -0.18f, -1.01f, -0.27f, -1.59f, -0.27f)
                curveToRelative(-0.58f, 0f, -1.11f, 0.09f, -1.59f, 0.27f)

                curveToRelative(-0.48f, 0.18f, -0.89f, 0.47f, -1.23f, 0.88f)
                curveToRelative(-0.34f, 0.41f, -0.6f, 0.93f, -0.79f, 1.59f)
                curveToRelative(-0.18f, 0.65f, -0.28f, 1.45f, -0.28f, 2.39f)
                verticalLineToRelative(1.92f)

                curveToRelative(0f, 0.94f, 0.09f, 1.74f, 0.28f, 2.39f)
                curveToRelative(0.19f, 0.66f, 0.45f, 1.19f, 0.8f, 1.6f)
                curveToRelative(0.34f, 0.41f, 0.75f, 0.71f, 1.23f, 0.89f)
                curveToRelative(0.48f, 0.18f, 1.01f, 0.28f, 1.59f, 0.28f)
                curveToRelative(0.59f, 0f, 1.12f, -0.09f, 1.59f, -0.28f)
                curveToRelative(0.48f, -0.18f, 0.88f, -0.48f, 1.22f, -0.89f)
                curveToRelative(0.34f, -0.41f, 0.6f, -0.94f, 0.78f, -1.6f)
                curveToRelative(0.18f, -0.65f, 0.28f, -1.45f, 0.28f, -2.39f)
                verticalLineToRelative(-1.92f)
                curveToRelative(0f, -0.94f, -0.09f, -1.74f, -0.28f, -2.39f)
                curveToRelative(-0.18f, -0.66f, -0.44f, -1.19f, -0.78f, -1.59f)
                close()
                moveTo(13.9f, 13.22f)
                curveToRelative(0f, 0.6f, -0.04f, 1.11f, -0.12f, 1.53f)
                curveToRelative(-0.08f, 0.42f, -0.2f, 0.76f, -0.36f, 1.02f)
                curveToRelative(-0.16f, 0.26f, -0.36f, 0.45f, -0.59f, 0.57f)
                curveToRelative(-0.23f, 0.12f, -0.51f, 0.18f, -0.82f, 0.18f)
                curveToRelative(-0.3f, 0f, -0.58f, -0.06f, -0.82f, -0.18f)
                reflectiveCurveToRelative(-0.44f, -0.31f, -0.6f, -0.57f)
                curveToRelative(-0.16f, -0.26f, -0.29f, -0.6f, -0.38f, -1.02f)
                curveToRelative(-0.09f, -0.42f, -0.13f, -0.93f, -0.13f, -1.53f)
                verticalLineToRelative(-2.5f)
                curveToRelative(0f, -0.6f, 0.04f, -1.11f, 0.13f, -1.52f)
                curveToRelative(0.09f, -0.41f, 0.21f, -0.74f, 0.38f, -1f)
                curveToRelative(0.16f, -0.25f, 0.36f, -0.43f, 0.6f, -0.55f)
                curveToRelative(0.24f, -0.11f, 0.51f, -0.17f, 0.81f, -0.17f)
                curveToRelative(0.31f, 0f, 0.58f, 0.06f, 0.81f, 0.17f)
                curveToRelative(0.24f, 0.11f, 0.44f, 0.29f, 0.6f, 0.55f)
                curveToRelative(0.16f, 0.25f, 0.29f, 0.58f, 0.37f, 0.99f)
                curveToRelative(0.08f, 0.41f, 0.13f, 0.92f, 0.13f, 1.52f)
                verticalLineToRelative(2.51f)
                close()
            }
        }
        return digit0!!
    }

private var digit0: ImageVector? = null

val Digit_4: ImageVector
    get() {
        if (digit4 != null) {
            return digit4!!
        }
        digit4 = materialIcon(name = "Digit 4") {

            materialPath {
                moveTo(16f,19f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(-6f)
                horizontalLineToRelative(-6f)
                verticalLineToRelative(-8f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(6f)
                horizontalLineToRelative(4f)
                verticalLineToRelative(-6f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(10f)
                close()
            }
        }
        return digit4!!
    }

private var digit4: ImageVector? = null

@Preview
@Composable
fun MyIconPreview() {
    NopeRemoteTheme {
        Icon(imageVector = Digit_0, contentDescription = null)
    }
}