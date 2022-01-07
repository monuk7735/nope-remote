package com.monuk7735.nope.remote.models.database

import android.os.Parcel
import android.os.Parcelable
import android.os.Vibrator
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.models.custom.flows.FlowTransmit
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "flows")
data class FlowDataDBModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val flowUnits: List<FlowTransmit>,
) : Parcelable {

    private companion object : Parceler<FlowDataDBModel> {
        override fun create(parcel: Parcel): FlowDataDBModel {
            val id = parcel.readInt()
            val name = parcel.readString()!!
            val flowUnits = Gson().fromJson<List<FlowTransmit>>(parcel.readString(),
                object : TypeToken<List<FlowTransmit>>() {}.type)
            return FlowDataDBModel(
                id,
                name,
                flowUnits
            )
        }

        override fun FlowDataDBModel.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeString(name)
            parcel.writeString(Gson().toJson(flowUnits))
        }

    }

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