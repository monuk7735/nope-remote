package com.monuk7735.nope.remote.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(0.dp)
)

class DPadShape(private val turnRadius: Dp, private val shortedLineDiff:Dp) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(
            path = Path().apply {
                reset()
//                arcTo(
//                    rect = Rect(
//                        left = 0f,
//                        top = 0f,
//                        right = turnRadius.value,
//                        bottom = turnRadius.value
//                    ),
//                    startAngleDegrees = 360f,
//                    sweepAngleDegrees = 120f,
//                    forceMoveTo = false
//                )
                moveTo(turnRadius.value,0f)
                lineTo(size.width - turnRadius.value, 0f)
                lineTo(size.width - shortedLineDiff.value, size.height)
                lineTo(shortedLineDiff.value, size.height)
                lineTo(0f,0f)
                close()

            },
        )
    }
}
