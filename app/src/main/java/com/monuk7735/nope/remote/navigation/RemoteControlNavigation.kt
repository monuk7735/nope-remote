package com.monuk7735.nope.remote.navigation

sealed class RemoteControlNavigation(val route: String) {

    object RemoteControlMain : RemoteControlNavigation("remote_control_main")

    object RemoteControlSettings : RemoteControlNavigation("remote_control_settings")

    object RemoteControlEditLayout : RemoteControlNavigation("remote_control_layout")

}
