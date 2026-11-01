package com.monuk7735.nope.remote.models.database

import android.os.Parcelable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Input
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.VolumeDown
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ArrowCircleDown
import androidx.compose.material.icons.outlined.ArrowCircleUp
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.FastRewind
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.ui.graphics.vector.ImageVector
import com.monuk7735.nope.remote.androidtv.proto.remote.Remotemessage
import kotlinx.parcelize.Parcelize

@Parcelize
data class AndroidTvButtonDBModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val keyCodeName: String,
    val iconName: String? = null,
    var gridCol: Int? = null,
    var gridRow: Int? = null,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
) : Parcelable {

    fun isReservedCorner(hCount: Int, vCount: Int): Boolean =
        (gridCol == 0 && gridRow == vCount - 1) ||
            (gridCol == hCount - 1 && gridRow == vCount - 1)

    fun isPlacedInGrid(hCount: Int, vCount: Int): Boolean = gridCol != null &&
        gridRow != null &&
        gridCol!! in 0 until hCount &&
        gridRow!! in 0 until vCount &&
        !isReservedCorner(hCount, vCount)

    fun getRemoteKeyCode(): Remotemessage.RemoteKeyCode? = try {
        Remotemessage.RemoteKeyCode.valueOf(keyCodeName)
    } catch (_: Exception) {
        null
    }

    fun isKeyboardAction(): Boolean = keyCodeName == "ACTION_KEYBOARD"

    fun getDigitValue(): Int? {
        val n = name.uppercase().replace("_", " ").trim()
        val k = keyCodeName.uppercase()
        return when {
            n == "0" || k == "KEYCODE_0" -> 0
            n == "1" || k == "KEYCODE_1" -> 1
            n == "2" || k == "KEYCODE_2" -> 2
            n == "3" || k == "KEYCODE_3" -> 3
            n == "4" || k == "KEYCODE_4" -> 4
            n == "5" || k == "KEYCODE_5" -> 5
            n == "6" || k == "KEYCODE_6" -> 6
            n == "7" || k == "KEYCODE_7" -> 7
            n == "8" || k == "KEYCODE_8" -> 8
            n == "9" || k == "KEYCODE_9" -> 9
            else -> null
        }
    }

    fun getTextIcon(): String? {
        val digit = getDigitValue()
        if (digit != null) return digit.toString()
        if (keyCodeName == "KEYCODE_DPAD_CENTER" || name.equals("OK", ignoreCase = true)) {
            return "OK"
        }
        if (name.startsWith("TEXT:", ignoreCase = true)) {
            return name.substringAfter(":")
        }
        return null
    }

    fun getIcon(): ImageVector? {
        if (getTextIcon() != null) return null
        return when (keyCodeName) {
            "ACTION_KEYBOARD" -> Icons.Outlined.Keyboard
            "KEYCODE_POWER" -> Icons.Outlined.PowerSettingsNew
            "KEYCODE_TV_POWER" -> Icons.Outlined.Tv
            "KEYCODE_HOME" -> Icons.Outlined.Home
            "KEYCODE_BACK" -> Icons.AutoMirrored.Outlined.ArrowBack
            "KEYCODE_MENU" -> Icons.Outlined.Menu
            "KEYCODE_SETTINGS" -> Icons.Outlined.Settings
            "KEYCODE_DPAD_UP" -> Icons.Outlined.KeyboardArrowUp
            "KEYCODE_DPAD_DOWN" -> Icons.Outlined.KeyboardArrowDown
            "KEYCODE_DPAD_LEFT" -> Icons.AutoMirrored.Outlined.KeyboardArrowLeft
            "KEYCODE_DPAD_RIGHT" -> Icons.AutoMirrored.Outlined.KeyboardArrowRight
            "KEYCODE_VOLUME_UP" -> Icons.AutoMirrored.Outlined.VolumeUp
            "KEYCODE_VOLUME_DOWN" -> Icons.AutoMirrored.Outlined.VolumeDown
            "KEYCODE_VOLUME_MUTE" -> Icons.AutoMirrored.Outlined.VolumeOff
            "KEYCODE_CHANNEL_UP" -> Icons.Outlined.ArrowCircleUp
            "KEYCODE_CHANNEL_DOWN" -> Icons.Outlined.ArrowCircleDown
            "KEYCODE_MEDIA_PLAY_PAUSE", "KEYCODE_MEDIA_PLAY" -> Icons.Outlined.PlayArrow
            "KEYCODE_MEDIA_PAUSE" -> Icons.Outlined.Pause
            "KEYCODE_MEDIA_STOP" -> Icons.Outlined.Stop
            "KEYCODE_MEDIA_REWIND" -> Icons.Outlined.FastRewind
            "KEYCODE_MEDIA_FAST_FORWARD" -> Icons.Outlined.FastForward
            "KEYCODE_MEDIA_PREVIOUS" -> Icons.Outlined.SkipPrevious
            "KEYCODE_MEDIA_NEXT" -> Icons.Outlined.SkipNext
            "KEYCODE_INFO" -> Icons.Outlined.Info
            "KEYCODE_GUIDE" -> Icons.Outlined.Map
            "KEYCODE_TV_INPUT" -> Icons.AutoMirrored.Outlined.Input
            "KEYCODE_VOICE_ASSIST" -> Icons.Outlined.Mic
            "KEYCODE_SEARCH" -> Icons.Outlined.Search
            "KEYCODE_CAPTIONS" -> Icons.Outlined.Subtitles
            "KEYCODE_APP_SWITCH" -> Icons.Outlined.Apps
            "KEYCODE_SLEEP" -> Icons.Outlined.Bedtime
            "KEYCODE_PROG_RED",
            "KEYCODE_PROG_GREEN",
            "KEYCODE_PROG_YELLOW",
            "KEYCODE_PROG_BLUE",
            -> Icons.Outlined.Circle
            else -> null
        }
    }
}
