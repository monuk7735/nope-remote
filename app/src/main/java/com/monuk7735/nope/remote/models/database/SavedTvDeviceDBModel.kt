package com.monuk7735.nope.remote.models.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "wifi_remotes",
    indices = [Index(value = ["host"], unique = true)],
)
data class SavedTvDeviceDBModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val host: String,
    val port: Int = 6466,
    val added: Date = Date(),
    val onScreenButtons: List<AndroidTvButtonDBModel>? = null,
    val offScreenButtons: List<AndroidTvButtonDBModel>? = null,
    val gridHCount: Int = 5,
    val gridVCount: Int = 10,
) : Parcelable {

    fun getEffectiveOnScreenButtons(): List<AndroidTvButtonDBModel> =
        onScreenButtons ?: AndroidTvButtonCatalog.getDefaultOnScreenButtons()

    fun getEffectiveOffScreenButtons(): List<AndroidTvButtonDBModel> =
        offScreenButtons ?: AndroidTvButtonCatalog.getDefaultOffScreenButtons()

    fun getAllDigits(): List<AndroidTvButtonDBModel> {
        val digits = mutableMapOf<Int, AndroidTvButtonDBModel>()
        getEffectiveOnScreenButtons().forEach { b -> b.getDigitValue()?.let { digits[it] = b } }
        getEffectiveOffScreenButtons().forEach { b -> b.getDigitValue()?.let { digits[it] = b } }
        if (digits.size < 10) return emptyList()
        return (0..9).mapNotNull { digits[it] }
    }

    fun getAllOffScreenNonDigits(): List<AndroidTvButtonDBModel> =
        getEffectiveOffScreenButtons().filter { it.getDigitValue() == null }
}
