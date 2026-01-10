package com.monuk7735.nope.remote.models.database

import android.os.Parcelable
import android.os.Vibrator
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.infrared.patterns.IRPattern
import com.monuk7735.nope.remote.ui.theme.icons.*
import kotlinx.parcelize.Parcelize

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
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
            return null
        }
        return getIconFromMap(n)
    }

    companion object {
        private fun getIconFromMap(name: String): ImageVector? {
            return when (name) {
                // Navigation
                "UP", "CURSOR UP", "DIRECTION UP", "ARROW UP" -> Icons.Outlined.KeyboardArrowUp
                "DOWN", "CURSOR DOWN", "DIRECTION DOWN", "ARROW DOWN" -> Icons.Outlined.KeyboardArrowDown
                "LEFT", "CURSOR LEFT", "DIRECTION LEFT", "ARROW LEFT" -> Icons.AutoMirrored.Outlined.KeyboardArrowLeft
                "RIGHT", "CURSOR RIGHT", "DIRECTION RIGHT", "ARROW RIGHT" -> Icons.AutoMirrored.Outlined.KeyboardArrowRight
                "OK", "ENTER", "SELECT", "CENTER" -> Icons.Outlined.CheckCircle
                "BACK", "RETURN", "PREV", "PREVIOUS" -> Icons.AutoMirrored.Outlined.ArrowBack

                // Menu / UI
                "MENU" -> Icons.Outlined.Menu
                "HOME", "HOMEPAGE" -> Icons.Outlined.Home
                "EXIT", "CLOSE", "CANCEL" -> Icons.Outlined.Close
                "INFO", "DISPLAY", "DISP", "OSD" -> Icons.Outlined.Info
                "GUIDE", "EPG" -> Icons.Outlined.Map
                "SETUP", "SETTINGS", "SETTING", "TOOL", "TOOLS", "OPT", "OPTION", "OPTIONS" -> Icons.Outlined.Settings
                "SEARCH" -> Icons.Outlined.Search

                // Power
                "POWER", "PWR", "POWER ON", "POWER OFF" -> Icons.Outlined.PowerSettingsNew

                // Volume
                "VOL+", "VOL +", "VOL UP", "VOLUME UP", "VOLUME+" -> Icons.AutoMirrored.Outlined.VolumeUp
                "VOL-", "VOL -", "VOL DOWN", "VOLUME DOWN", "VOLUME-" -> Icons.AutoMirrored.Outlined.VolumeDown
                "MUTE" -> Icons.AutoMirrored.Outlined.VolumeOff

                // Channel
                "CH+", "CH +", "CH UP", "CHANNEL UP", "CHANNEL+", "PROG+", "PROG UP", "PAGE+", "PAGE UP" -> Icons.Outlined.ArrowCircleUp
                "CH-", "CH -", "CH DOWN", "CHANNEL DOWN", "CHANNEL-", "PROG-", "PROG DOWN", "PAGE-", "PAGE DOWN" -> Icons.Outlined.ArrowCircleDown
                "PREV CH", "PREVIOUS CHANNEL", "LAST" -> Icons.AutoMirrored.Outlined.ArrowBack

                // Media
                "PLAY" -> Icons.Outlined.PlayArrow
                "PAUSE" -> Icons.Outlined.Pause
                "STOP" -> Icons.Outlined.Stop
                "REW", "REV", "REWIND", "BACKWARD" -> Icons.Outlined.FastRewind
                "FWD", "FORWARD", "FAST FORWARD" -> Icons.Outlined.FastForward
                "NEXT", "SKIP NEXT", "SKIP+" -> Icons.Outlined.SkipNext
                "PREV TRACK", "SKIP PREV", "SKIP-" -> Icons.Outlined.SkipPrevious
                "REC", "RECORD" -> Icons.Outlined.FiberManualRecord

                // Input
                "INPUT", "SOURCE", "HDMI", "AV", "AUX" -> Icons.AutoMirrored.Outlined.Input

                // Color
                "RED" -> Icons.Outlined.Circle
                "GREEN" -> Icons.Outlined.Circle
                "YELLOW" -> Icons.Outlined.Circle
                "BLUE" -> Icons.Outlined.Circle

                // New Standards
                "SUBTITLE", "SUB", "CAPTION", "CC" -> Icons.Outlined.Subtitles
                "AUDIO", "LANG", "LANGUAGE" -> Icons.Outlined.Audiotrack
                "ASPECT", "RATIO", "ZOOM", "SIZE", "WIDE" -> Icons.Outlined.AspectRatio
                "LIST", "CH LIST", "CHANNEL LIST" -> Icons.AutoMirrored.Outlined.List
                "FAV", "FAVORITE", "FAVORITES" -> Icons.Outlined.Favorite
                "SLEEP", "TIMER" -> Icons.Outlined.Bedtime

                else -> null
            }
        }
    }

    fun transmit(
        irController: IRController?,
        vibrator: Vibrator,
    ) {
        irController?.transmit(
            irPattern = irPattern,
            vibrator = vibrator
        )
    }
}