package com.monuk7735.nope.remote.models.database

import android.os.Parcelable
import android.os.Vibrator
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.models.custom.flows.FlowTransmit
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "flows")
data class FlowDataDBModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val flowUnits: List<FlowTransmit>
) : Parcelable {
    suspend fun execute(
        irController: IRController,
        vibrator: Vibrator,
    ) {
        flowUnits.forEach {
            irController.transmit(
                it.irPattern,
                vibrator
            )
            delay(500)
        }
    }
}