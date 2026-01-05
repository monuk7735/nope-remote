package com.monuk7735.nope.remote.models.database

import android.os.Parcelable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.DeveloperBoard
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.ModeFanOff
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.Videocam

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*
import com.monuk7735.nope.remote.models.CustomRemoteLayout

@Parcelize
@Entity(tableName = "remotes")
data class RemoteDataDBModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val brand: String,
    val type: String,
    val added: Date,
    val onScreenRemoteButtonDBS: List<RemoteButtonDBModel>,
    val offScreenRemoteButtonDBS: List<RemoteButtonDBModel>,
    val preferCustomUi: Boolean = true,
) : Parcelable {
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

    fun getByName(searchName: String): RemoteButtonDBModel? {
        onScreenRemoteButtonDBS.forEach {
            if (searchName.uppercase() == it.name.uppercase()) {
                return it
            }
        }

        offScreenRemoteButtonDBS.forEach {
            if (searchName.uppercase() == it.name.uppercase()) {
                return it
            }
        }

        return null
    }

    fun getAllDigits(): List<RemoteButtonDBModel> {
        val digits = mutableMapOf<Int, RemoteButtonDBModel>()
        
        onScreenRemoteButtonDBS.forEach { button ->
            button.getDigitValue()?.let { digits[it] = button }
        }
        
        offScreenRemoteButtonDBS.forEach { button ->
            button.getDigitValue()?.let { digits[it] = button }
        }

        if (digits.size < 10) return emptyList()

        return (0..9).map { digits[it]!! }
    }

    fun getAllOffScreen(): List<RemoteButtonDBModel> {
        val toRet = mutableListOf<RemoteButtonDBModel>()

        offScreenRemoteButtonDBS.forEach { button ->
            if (button.getDigitValue() == null) {
                toRet.add(button)
            }
        }

        return toRet
    }

    fun getCustomLayoutType(): CustomRemoteLayout {
        val isCamera = type.contains("Camera", ignoreCase = true) ||
                type.contains("DSLR", ignoreCase = true)
        val shutterButton = getByName("SHUTTER")

        if (isCamera && shutterButton != null) {
            return CustomRemoteLayout.CAMERA
        }

        val isLight = type.contains("Light", ignoreCase = true) ||
                type.contains("Lamp", ignoreCase = true) ||
                type.contains("LED", ignoreCase = true) ||
                type.contains("Strip", ignoreCase = true)
        
        // Essential buttons for Light UI
        val hasColor = getByName("COLOR0") != null
        val hasBrightness = getByName("BRIGHTNESS+") != null

        if (isLight && (hasColor || hasBrightness)) {
            return CustomRemoteLayout.LIGHT
        }

        return CustomRemoteLayout.NONE
    }
}