package com.monuk7735.nope.remote.backup

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.models.database.SavedTvDeviceDBModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NopeBackupSerializer {

    private val dateTypeAdapter = object : TypeAdapter<Date>() {
        private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
        private val simpleIsoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

        override fun write(out: JsonWriter, value: Date?) {
            if (value == null) {
                out.nullValue()
            } else {
                out.value(value.time)
            }
        }

        override fun read(reader: JsonReader): Date? {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull()
                return null
            }
            if (reader.peek() == JsonToken.NUMBER) {
                return Date(reader.nextLong())
            }
            val str = reader.nextString()
            str.toLongOrNull()?.let { return Date(it) }
            return try {
                isoFormat.parse(str)
            } catch (_: Exception) {
                try {
                    simpleIsoFormat.parse(str)
                } catch (_: Exception) {
                    try {
                        DateFormat.getDateTimeInstance().parse(str)
                    } catch (_: Exception) {
                        try {
                            DateFormat.getDateTimeInstance(
                                DateFormat.DEFAULT,
                                DateFormat.DEFAULT,
                                Locale.US,
                            ).parse(str)
                        } catch (_: Exception) {
                            Date()
                        }
                    }
                }
            }
        }
    }

    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, dateTypeAdapter)
        .setPrettyPrinting()
        .create()

    fun serialize(backup: NopeBackup): String = gson.toJson(backup)

    fun deserialize(jsonString: String): NopeBackup {
        if (jsonString.isBlank()) {
            throw IllegalArgumentException("Backup file is empty")
        }
        val backup = try {
            gson.fromJson(jsonString, NopeBackup::class.java)
        } catch (e: Exception) {
            throw JsonParseException("Failed to parse backup JSON: ${e.message}", e)
        } ?: throw IllegalArgumentException("Backup file contained invalid or null data")

        if (backup.version <= 0) {
            throw IllegalArgumentException("Invalid backup format: missing or invalid version")
        }
        return backup
    }

    fun stripLayoutFromRemote(remote: RemoteDataDBModel): RemoteDataDBModel = remote.copy(
        onScreenRemoteButtonDBS = remote.onScreenRemoteButtonDBS.map {
            it.copy(offsetX = 0f, offsetY = 0f, gridCol = null, gridRow = null)
        },
        offScreenRemoteButtonDBS = remote.offScreenRemoteButtonDBS.map {
            it.copy(offsetX = 0f, offsetY = 0f, gridCol = null, gridRow = null)
        },
        gridHCount = 5,
        gridVCount = 10,
    )

    fun stripLayoutFromWifiRemote(wifiRemote: SavedTvDeviceDBModel): SavedTvDeviceDBModel =
        wifiRemote.copy(
            onScreenButtons = null,
            offScreenButtons = null,
            gridHCount = 5,
            gridVCount = 10,
        )
}
