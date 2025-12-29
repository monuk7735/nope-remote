package com.monuk7735.nope.remote.models.database

import android.os.Parcelable
import android.os.Vibrator
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.infrared.patterns.IRPattern
import com.monuk7735.nope.remote.ui.theme.icons.*
import kotlinx.parcelize.Parcelize

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Input
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.VolumeDown
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

@Parcelize
data class RemoteButtonDBModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    var offsetX: Float,
    var offsetY: Float,
    val name: String,
    val irPattern: IRPattern,
) : Parcelable {
    fun getDigitValue(): Int? {
        val n = name.uppercase()
        return when {
            n == "0" || n.contains("DIGIT 0") || n.contains("NUMBER 0") || n.contains("KEY 0") || n.contains("KEY_0") -> 0
            n == "1" || n.contains("DIGIT 1") || n.contains("NUMBER 1") || n.contains("KEY 1") || n.contains("KEY_1") -> 1
            n == "2" || n.contains("DIGIT 2") || n.contains("NUMBER 2") || n.contains("KEY 2") || n.contains("KEY_2") -> 2
            n == "3" || n.contains("DIGIT 3") || n.contains("NUMBER 3") || n.contains("KEY 3") || n.contains("KEY_3") -> 3
            n == "4" || n.contains("DIGIT 4") || n.contains("NUMBER 4") || n.contains("KEY 4") || n.contains("KEY_4") -> 4
            n == "5" || n.contains("DIGIT 5") || n.contains("NUMBER 5") || n.contains("KEY 5") || n.contains("KEY_5") -> 5
            n == "6" || n.contains("DIGIT 6") || n.contains("NUMBER 6") || n.contains("KEY 6") || n.contains("KEY_6") -> 6
            n == "7" || n.contains("DIGIT 7") || n.contains("NUMBER 7") || n.contains("KEY 7") || n.contains("KEY_7") -> 7
            n == "8" || n.contains("DIGIT 8") || n.contains("NUMBER 8") || n.contains("KEY 8") || n.contains("KEY_8") -> 8
            n == "9" || n.contains("DIGIT 9") || n.contains("NUMBER 9") || n.contains("KEY 9") || n.contains("KEY_9") -> 9
            else -> null
        }
    }

    fun getTextIcon(): String? {
        val n = name.uppercase()
        val digit = getDigitValue()
        if (digit != null) {
            return digit.toString()
        }
        if (n.startsWith("TEXT:")) {
            return name.substringAfter("TEXT:")
        }
        return null
    }

    fun getIcon(): ImageVector? {
        val n = name.uppercase()
        if (getTextIcon() != null) {
            return null // Fallback to text for digits and custom text
        }
        return when {
            // Specific overrides for risky partial matches
            n.contains("SETUP") -> Icons.Outlined.Settings

            // Navigation & UI
            (n.contains("BACK") && !n.contains("PLAYBACK")) || n.contains("RETURN") || n.contains("PREV") && !n.contains("SKIP") -> Icons.AutoMirrored.Outlined.ArrowBack
            n.contains("EXIT") || n.contains("CLOSE") || n.contains("CANCEL") -> Icons.Outlined.Close
            n.contains("MENU") -> Icons.Outlined.Menu
            n.contains("HOME") -> Icons.Outlined.Home
            n.contains("INFO") || n.contains("DISPLAY") || n.contains("DISP") -> Icons.Outlined.Info
            n.contains("GUIDE") || n.contains("EPG") -> Icons.Outlined.Map
            n.contains("SETTING") || n.contains("TOOL") || n.contains("OPTION") -> Icons.Outlined.Settings
            n.contains("SEARCH") && !n.contains("RESEARCH") -> Icons.Outlined.Search

            // Directions
            n == "UP" || n.contains("CURSOR UP") || n.contains("DIRECTION UP") || n.contains("ARROW UP") -> Icons.Outlined.KeyboardArrowUp
            n == "DOWN" || n.contains("CURSOR DOWN") || n.contains("DIRECTION DOWN") || n.contains("ARROW DOWN") -> Icons.Outlined.KeyboardArrowDown
            n == "LEFT" || n.contains("CURSOR LEFT") || n.contains("DIRECTION LEFT") || n.contains("ARROW LEFT") -> Icons.AutoMirrored.Outlined.KeyboardArrowLeft
            n == "RIGHT" || n.contains("CURSOR RIGHT") || n.contains("DIRECTION RIGHT") || n.contains("ARROW RIGHT") -> Icons.AutoMirrored.Outlined.KeyboardArrowRight
            n.contains("OK") || n.contains("ENTER") || n.contains("SELECT") -> Icons.Outlined.CheckCircle

            // Power
            n.contains("POWER") || n.contains("PWR") -> Icons.Outlined.PowerSettingsNew

            // Volume
            n.contains("MUTE") -> Icons.AutoMirrored.Outlined.VolumeOff
            (n.contains("VOL") && (n.contains("+") || n.contains("UP") || n.contains("INC"))) -> Icons.AutoMirrored.Outlined.VolumeUp
            (n.contains("VOL") && (n.contains("-") || n.contains("DOWN") || n.contains("DN") || n.contains("DEC"))) -> Icons.AutoMirrored.Outlined.VolumeDown

            // Channel / Page
            (n.contains("CH") || n.contains("CHAN") || n.contains("PROG") || n.contains("PAGE")) && (n.contains("+") || n.contains("UP") || n.contains("INC") || n.contains("NEXT")) -> Icons.Outlined.ArrowCircleUp
            (n.contains("CH") || n.contains("CHAN") || n.contains("PROG") || n.contains("PAGE")) && (n.contains("-") || n.contains("DOWN") || n.contains("DN") || n.contains("DEC") || n.contains("PREV")) -> Icons.Outlined.ArrowCircleDown

            // Fallback Navigation
            n.contains("UP") && !n.contains("VOL") && !n.contains("CH") && !n.contains("PAGE") -> Icons.Outlined.KeyboardArrowUp
            (n.contains("DOWN") || n.contains("DN")) && !n.contains("VOL") && !n.contains("CH") && !n.contains("PAGE") -> Icons.Outlined.KeyboardArrowDown
            n.contains("LEFT") -> Icons.AutoMirrored.Outlined.KeyboardArrowLeft
            n.contains("RIGHT") && !n.contains("BRIGHT") && !n.contains("LIGHT") -> Icons.AutoMirrored.Outlined.KeyboardArrowRight

            // Media
            n.contains("PLAY") && !n.contains("DISPLAY") -> Icons.Outlined.PlayArrow
            n.contains("PAUSE") -> Icons.Outlined.Pause
            n.contains("STOP") -> Icons.Outlined.Stop
            n.contains("REW") || n.contains("REV") -> Icons.Outlined.FastRewind
            n.contains("FWD") || n.contains("FORWARD") -> Icons.Outlined.FastForward
            n.contains("NEXT") || n.contains("SKIP") -> Icons.Outlined.SkipNext
            n.contains("REC") && !n.contains("RECENT") -> Icons.Outlined.FiberManualRecord

            // Inputs
            n.contains("INPUT") || n.contains("SOURCE") || n.contains("HDMI") || n.contains("AV") || n.contains("AUX") -> Icons.AutoMirrored.Outlined.Input

            // Colors
            n.contains("RED") && !n.contains("REDUCE") -> Icons.Outlined.Circle
            n.contains("GREEN") -> Icons.Outlined.Circle
            n.contains("YELLOW") -> Icons.Outlined.Circle
            n.contains("BLUE") -> Icons.Outlined.Circle
            
            else -> null // No icon found
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