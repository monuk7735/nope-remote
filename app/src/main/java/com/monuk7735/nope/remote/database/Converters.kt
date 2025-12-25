package com.monuk7735.nope.remote.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.monuk7735.nope.remote.models.custom.macros.MacroTransmit
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel
import java.util.*

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromListRemoteButtonDBModel(data: List<RemoteButtonDBModel>): String {
        return gson.toJson(data)
    }

    @TypeConverter
    fun toListRemoteButtonDBModel(data: String): List<RemoteButtonDBModel> {
        return gson.fromJson(data, object : TypeToken<List<RemoteButtonDBModel>>() {}.type)
    }

    @TypeConverter
    fun fromListOfMacroTransmit(data: List<MacroTransmit>): String {
        return gson.toJson(data)
    }

    @TypeConverter
    fun toListOfMacroTransmit(data: String): List<MacroTransmit> {
        return gson.fromJson(data, object : TypeToken<List<MacroTransmit>>() {}.type)
    }

    @TypeConverter
    fun fromDate(data: Date): String {
        return gson.toJson(data)
    }

    @TypeConverter
    fun toDate(data: String): Date {
        return gson.fromJson(data, object : TypeToken<Date>() {}.type)
    }

//    @TypeConverter
//    fun fromBitmap(data: Bitmap): String {
//        return gson.toJson(data)
//    }
//
//    @TypeConverter
//    fun toBitmap(data: String): Bitmap {
//        return gson.fromJson(data, object : TypeToken<Bitmap>() {}.type)
//    }

}