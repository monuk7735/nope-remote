package com.monuk7735.nope.remote.models.database

import android.os.Parcelable
import android.os.Vibrator
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.models.custom.macros.MacroTransmit
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "macros")
data class MacroDataDBModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val macroUnits: List<MacroTransmit>
) : Parcelable {
    suspend fun execute(
        irController: IRController?,
        vibrator: Vibrator,
    ) {
        macroUnits.forEach {
            irController?.transmit(
                it.irPattern,
                vibrator
            )
            delay(500)
        }
    }
}
