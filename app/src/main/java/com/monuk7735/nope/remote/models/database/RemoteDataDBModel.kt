package com.monuk7735.nope.remote.models.database

import android.os.Parcelable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monuk7735.nope.remote.ui.theme.icons.SetTopBox
import com.monuk7735.nope.remote.ui.theme.icons.TV
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
        return when (type) {
            "TV" -> TV
            "Set-Top Box" -> SetTopBox
            else -> Icons.Outlined.Delete
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