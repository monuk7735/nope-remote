package com.monuk7735.nope.remote.models.database

import android.os.Vibrator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.infrared.patterns.IRPattern
import com.monuk7735.nope.remote.ui.theme.icons.*

class RemoteButtonDBModel(
    var offsetX: Float,
    var offsetY: Float,
    val name: String,
    val irPattern: IRPattern,
) {
    fun getIcon(): ImageVector {
        return when (name) {
            "BACK" -> Icons.Outlined.ArrowBack
//            "CHANNEL DOWN" -> Icons.Outlined.KeyboardArrowDown
//            "CHANNEL UP" -> Icons.Outlined.KeyboardArrowUp
            "DIGIT 0" -> Digit_0
            "DIGIT 4" -> Digit_4
            "POWER TOGGLE" -> Power
            "EXIT" -> Exit
            "CURSOR LEFT" -> Icons.Outlined.KeyboardArrowLeft
            "CURSOR RIGHT" -> Icons.Outlined.KeyboardArrowRight
            "CURSOR UP" -> Icons.Outlined.KeyboardArrowUp
            "CURSOR DOWN" -> Icons.Outlined.KeyboardArrowDown
            "CURSOR ENTER" -> Icons.Outlined.Check
            "MENU" -> Icons.Outlined.Menu
            "VOLUME UP" -> Add
            "VOLUME DOWN" -> Subtract
            "FAVORITE" -> Icons.Outlined.FavoriteBorder

            else -> Icons.Outlined.DateRange
        }
    }

    fun transmit(
        irController: IRController,
        vibrator: Vibrator,
    ) {
        irController.transmit(
            irPattern = irPattern,
            vibrator = vibrator
        )
    }
}