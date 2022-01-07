package com.monuk7735.nope.remote.models.database

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.monuk7735.nope.remote.composables.RemoteButtonSingle
import com.monuk7735.nope.remote.ui.theme.icons.SetTopBox
import com.monuk7735.nope.remote.ui.theme.icons.TV
import kotlinx.parcelize.Parceler
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

    private companion object : Parceler<RemoteDataDBModel> {
        override fun create(parcel: Parcel): RemoteDataDBModel {
            val id = parcel.readInt()
            val name = parcel.readString()!!
            val brand = parcel.readString()!!
            val type = parcel.readString()!!
            val date =
                Gson().fromJson<Date>(parcel.readString(), object : TypeToken<Date>() {}.type)
            val onScreenRemoteButtonDBS =
                Gson().fromJson<List<RemoteButtonDBModel>>(parcel.readString(),
                    object : TypeToken<List<RemoteButtonDBModel>>() {}.type)
            val offScreenRemoteButtonDBS =
                Gson().fromJson<List<RemoteButtonDBModel>>(parcel.readString(),
                    object : TypeToken<List<RemoteButtonDBModel>>() {}.type)

            return RemoteDataDBModel(
                id,
                name,
                brand,
                type,
                date,
                onScreenRemoteButtonDBS,
                offScreenRemoteButtonDBS
            )
        }

        override fun RemoteDataDBModel.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeString(name)
            parcel.writeString(brand)
            parcel.writeString(type)
            parcel.writeString(Gson().toJson(added))
            parcel.writeString(Gson().toJson(onScreenRemoteButtonDBS))
            parcel.writeString(Gson().toJson(offScreenRemoteButtonDBS))
        }

    }

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

//    fun getAllOffScreenComposable(): List<@Composable () -> Unit> {
//        val toRet = mutableListOf<@Composable () -> Unit>()
//
//        val digitsNames = listOf(
//            "DIGIT 0",
//            "DIGIT 1",
//            "DIGIT 2",
//            "DIGIT 3",
//            "DIGIT 4",
//            "DIGIT 5",
//            "DIGIT 6",
//            "DIGIT 7",
//            "DIGIT 8",
//            "DIGIT 9",
//        )
//
//        offScreenRemoteButtonDBS.forEach {
//            if (!digitsNames.contains(it.name))
//                toRet.add {
//                    RemoteButtonSingle(
//                        name = it.name,
//                        icon = it.getIcon(),
//                        offsetX = it.offsetX,
//                        offsetY = it.offsetY,
//                        onClick = {
//
//                        }
//                    )
//                }
//        }
//
//        return toRet
//    }

}