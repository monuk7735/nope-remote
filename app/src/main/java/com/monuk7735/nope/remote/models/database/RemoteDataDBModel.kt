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
        val toRet = mutableListOf<RemoteButtonDBModel>()
        val digitsNames = listOf(
            "DIGIT 0",
            "DIGIT 1",
            "DIGIT 2",
            "DIGIT 3",
            "DIGIT 4",
            "DIGIT 5",
            "DIGIT 6",
            "DIGIT 7",
            "DIGIT 8",
            "DIGIT 9",
        )
        digitsNames.forEach {
            val temp = getByName(it) ?: return emptyList()
            toRet.add(temp)
        }
        return toRet
    }

    fun getAllOffScreen(): List<RemoteButtonDBModel> {
        val toRet = mutableListOf<RemoteButtonDBModel>()

        val digitsNames = listOf(
            "DIGIT 0",
            "DIGIT 1",
            "DIGIT 2",
            "DIGIT 3",
            "DIGIT 4",
            "DIGIT 5",
            "DIGIT 6",
            "DIGIT 7",
            "DIGIT 8",
            "DIGIT 9",
        )

        offScreenRemoteButtonDBS.forEach {
//            if (!digitsNames.contains(it.name))
            toRet.add(it)
        }

        return toRet
    }
}