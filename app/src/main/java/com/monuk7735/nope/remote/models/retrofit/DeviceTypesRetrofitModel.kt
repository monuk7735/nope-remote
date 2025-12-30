package com.monuk7735.nope.remote.models.retrofit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.DeveloperBoard
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.ModeFanOff
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.ui.graphics.vector.ImageVector

data class DeviceTypesRetrofitModel(
    val type: String,
) {
    fun getIcon(): ImageVector {
        val t = type.uppercase()
        return when {
            t.contains("TV") || t.contains("TELEVISION") -> Icons.Outlined.Tv
            t.contains("BOX") || t.contains("CABLE") || t.contains("SAT") || t.contains("ROUTER") || t.contains("DVR") -> Icons.Outlined.Router
            t.contains("PROJ") -> Icons.Outlined.Videocam
            t.contains("AUDIO") || t.contains("RECEIVER") || t.contains("AMP") || t.contains("SOUND") || t.contains("SPEAKER") || t.contains("HIFI") -> Icons.Outlined.Speaker
            t.contains("FAN") -> Icons.Outlined.ModeFanOff
            t.contains("AC") || t.contains("AIR") || t.contains("CLIMATE") -> Icons.Outlined.AcUnit
            t.contains("LIGHT") || t.contains("LAMP") -> Icons.Outlined.Lightbulb
            t.contains("CAM") || t.contains("DSLR") -> Icons.Outlined.CameraAlt
            t.contains("DVD") || t.contains("CD") || t.contains("BLU") || t.contains("DISC") -> Icons.Outlined.Videocam

            else -> Icons.Outlined.DeveloperBoard
        }
    }
}