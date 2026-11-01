package com.monuk7735.nope.remote.models.database

object AndroidTvButtonCatalog {

    fun createAllAvailableButtons(): List<AndroidTvButtonDBModel> = listOf(
        // Power & System
        AndroidTvButtonDBModel(name = "Power", keyCodeName = "KEYCODE_POWER"),
        AndroidTvButtonDBModel(name = "TV Power", keyCodeName = "KEYCODE_TV_POWER"),
        AndroidTvButtonDBModel(name = "Sleep", keyCodeName = "KEYCODE_SLEEP"),
        AndroidTvButtonDBModel(name = "Keyboard", keyCodeName = "ACTION_KEYBOARD"),
        AndroidTvButtonDBModel(name = "Settings", keyCodeName = "KEYCODE_SETTINGS"),
        AndroidTvButtonDBModel(name = "Input", keyCodeName = "KEYCODE_TV_INPUT"),
        AndroidTvButtonDBModel(name = "Info", keyCodeName = "KEYCODE_INFO"),
        AndroidTvButtonDBModel(name = "Guide", keyCodeName = "KEYCODE_GUIDE"),
        AndroidTvButtonDBModel(name = "Menu", keyCodeName = "KEYCODE_MENU"),
        AndroidTvButtonDBModel(name = "Apps", keyCodeName = "KEYCODE_APP_SWITCH"),

        // Navigation
        AndroidTvButtonDBModel(name = "Up", keyCodeName = "KEYCODE_DPAD_UP"),
        AndroidTvButtonDBModel(name = "Down", keyCodeName = "KEYCODE_DPAD_DOWN"),
        AndroidTvButtonDBModel(name = "Left", keyCodeName = "KEYCODE_DPAD_LEFT"),
        AndroidTvButtonDBModel(name = "Right", keyCodeName = "KEYCODE_DPAD_RIGHT"),
        AndroidTvButtonDBModel(name = "OK", keyCodeName = "KEYCODE_DPAD_CENTER"),
        AndroidTvButtonDBModel(name = "Back", keyCodeName = "KEYCODE_BACK"),
        AndroidTvButtonDBModel(name = "Home", keyCodeName = "KEYCODE_HOME"),

        // Volume & Audio
        AndroidTvButtonDBModel(name = "Volume Up", keyCodeName = "KEYCODE_VOLUME_UP"),
        AndroidTvButtonDBModel(name = "Volume Down", keyCodeName = "KEYCODE_VOLUME_DOWN"),
        AndroidTvButtonDBModel(name = "Mute", keyCodeName = "KEYCODE_VOLUME_MUTE"),

        // Channels
        AndroidTvButtonDBModel(name = "Channel Up", keyCodeName = "KEYCODE_CHANNEL_UP"),
        AndroidTvButtonDBModel(name = "Channel Down", keyCodeName = "KEYCODE_CHANNEL_DOWN"),

        // Media Controls
        AndroidTvButtonDBModel(name = "Play/Pause", keyCodeName = "KEYCODE_MEDIA_PLAY_PAUSE"),
        AndroidTvButtonDBModel(name = "Play", keyCodeName = "KEYCODE_MEDIA_PLAY"),
        AndroidTvButtonDBModel(name = "Pause", keyCodeName = "KEYCODE_MEDIA_PAUSE"),
        AndroidTvButtonDBModel(name = "Stop", keyCodeName = "KEYCODE_MEDIA_STOP"),
        AndroidTvButtonDBModel(name = "Rewind", keyCodeName = "KEYCODE_MEDIA_REWIND"),
        AndroidTvButtonDBModel(
            name = "Fast Forward",
            keyCodeName = "KEYCODE_MEDIA_FAST_FORWARD",
        ),
        AndroidTvButtonDBModel(name = "Previous", keyCodeName = "KEYCODE_MEDIA_PREVIOUS"),
        AndroidTvButtonDBModel(name = "Next", keyCodeName = "KEYCODE_MEDIA_NEXT"),

        // Voice & Search
        AndroidTvButtonDBModel(name = "Assistant", keyCodeName = "KEYCODE_VOICE_ASSIST"),
        AndroidTvButtonDBModel(name = "Search", keyCodeName = "KEYCODE_SEARCH"),

        // Captions
        AndroidTvButtonDBModel(name = "Captions", keyCodeName = "KEYCODE_CAPTIONS"),

        // Color buttons
        AndroidTvButtonDBModel(name = "Red", keyCodeName = "KEYCODE_PROG_RED"),
        AndroidTvButtonDBModel(name = "Green", keyCodeName = "KEYCODE_PROG_GREEN"),
        AndroidTvButtonDBModel(name = "Yellow", keyCodeName = "KEYCODE_PROG_YELLOW"),
        AndroidTvButtonDBModel(name = "Blue", keyCodeName = "KEYCODE_PROG_BLUE"),

        // Digits
        AndroidTvButtonDBModel(name = "0", keyCodeName = "KEYCODE_0"),
        AndroidTvButtonDBModel(name = "1", keyCodeName = "KEYCODE_1"),
        AndroidTvButtonDBModel(name = "2", keyCodeName = "KEYCODE_2"),
        AndroidTvButtonDBModel(name = "3", keyCodeName = "KEYCODE_3"),
        AndroidTvButtonDBModel(name = "4", keyCodeName = "KEYCODE_4"),
        AndroidTvButtonDBModel(name = "5", keyCodeName = "KEYCODE_5"),
        AndroidTvButtonDBModel(name = "6", keyCodeName = "KEYCODE_6"),
        AndroidTvButtonDBModel(name = "7", keyCodeName = "KEYCODE_7"),
        AndroidTvButtonDBModel(name = "8", keyCodeName = "KEYCODE_8"),
        AndroidTvButtonDBModel(name = "9", keyCodeName = "KEYCODE_9"),
    )

    fun getDefaultOnScreenButtons(): List<AndroidTvButtonDBModel> = listOf(
        AndroidTvButtonDBModel(
            name = "Power",
            keyCodeName = "KEYCODE_POWER",
            gridCol = 0,
            gridRow = 0,
        ),
        AndroidTvButtonDBModel(
            name = "Keyboard",
            keyCodeName = "ACTION_KEYBOARD",
            gridCol = 4,
            gridRow = 0,
        ),
        AndroidTvButtonDBModel(
            name = "Up",
            keyCodeName = "KEYCODE_DPAD_UP",
            gridCol = 2,
            gridRow = 2,
        ),
        AndroidTvButtonDBModel(
            name = "Left",
            keyCodeName = "KEYCODE_DPAD_LEFT",
            gridCol = 1,
            gridRow = 3,
        ),
        AndroidTvButtonDBModel(
            name = "OK",
            keyCodeName = "KEYCODE_DPAD_CENTER",
            gridCol = 2,
            gridRow = 3,
        ),
        AndroidTvButtonDBModel(
            name = "Right",
            keyCodeName = "KEYCODE_DPAD_RIGHT",
            gridCol = 3,
            gridRow = 3,
        ),
        AndroidTvButtonDBModel(
            name = "Down",
            keyCodeName = "KEYCODE_DPAD_DOWN",
            gridCol = 2,
            gridRow = 4,
        ),
        AndroidTvButtonDBModel(
            name = "Back",
            keyCodeName = "KEYCODE_BACK",
            gridCol = 1,
            gridRow = 6,
        ),
        AndroidTvButtonDBModel(
            name = "Home",
            keyCodeName = "KEYCODE_HOME",
            gridCol = 2,
            gridRow = 6,
        ),
        AndroidTvButtonDBModel(
            name = "Menu",
            keyCodeName = "KEYCODE_MENU",
            gridCol = 3,
            gridRow = 6,
        ),
        AndroidTvButtonDBModel(
            name = "Volume Up",
            keyCodeName = "KEYCODE_VOLUME_UP",
            gridCol = 1,
            gridRow = 8,
        ),
        AndroidTvButtonDBModel(
            name = "Mute",
            keyCodeName = "KEYCODE_VOLUME_MUTE",
            gridCol = 2,
            gridRow = 8,
        ),
        AndroidTvButtonDBModel(
            name = "Volume Down",
            keyCodeName = "KEYCODE_VOLUME_DOWN",
            gridCol = 3,
            gridRow = 8,
        ),
    )

    fun getDefaultOffScreenButtons(): List<AndroidTvButtonDBModel> {
        val defaultOnScreenKeyCodes = getDefaultOnScreenButtons().map { it.keyCodeName }.toSet()
        return createAllAvailableButtons().filter { it.keyCodeName !in defaultOnScreenKeyCodes }
    }
}
