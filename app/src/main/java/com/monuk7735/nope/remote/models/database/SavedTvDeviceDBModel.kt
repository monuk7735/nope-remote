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
) : Parcelable
