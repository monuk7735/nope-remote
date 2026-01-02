package com.monuk7735.nope.remote.repository

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.MutableLiveData
import com.monuk7735.nope.remote.infrared.IrCsvParser
import com.monuk7735.nope.remote.models.retrofit.DeviceBrandsRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceCodesRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceTypesRetrofitModel
import com.monuk7735.nope.remote.service.RepoDownloadManager
import java.io.File

class IrextRepository(private val application: Application) : IRSourceRepository {

    private var sqliteDb: SQLiteDatabase? = null
    // Dynamic path logic in ensureDb()

    private fun ensureDb(): Boolean {
        if (sqliteDb != null && sqliteDb?.isOpen == true) return true

        val context = application
        // Use directory from Enum
        val dirName = RepoDownloadManager.RepositoryInfo.IREXT.directoryName
        val repoDir = File(context.filesDir, "repos/$dirName")
        
        val dbFile = File(repoDir, "irext_db_20251031_sqlite3.db")
        var genericDbFile = if (dbFile.exists()) dbFile else null
        
        if (genericDbFile == null && repoDir.exists()) {
             genericDbFile = repoDir.walk().find { it.extension == "db" || it.extension == "sqlite" }
        }

        if (genericDbFile != null && genericDbFile.exists()) {
            if (genericDbFile.length() < 1024 * 10) {
                return false
            }
            
            try {
                sqliteDb = SQLiteDatabase.openDatabase(
                    genericDbFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY
                )
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                // Do not delete file on open error. It might be locked or transient.
            }
        }
        return false
    }

    override fun isRepoInstalled(): Boolean {
        return ensureDb()
    }

    override suspend fun getTypes(): List<DeviceTypesRetrofitModel> {
        if (ensureDb()) {
            val list = mutableListOf<DeviceTypesRetrofitModel>()
            try {
                val cursor = sqliteDb?.rawQuery("SELECT DISTINCT name_en FROM category WHERE name_en != 'AC' ORDER BY name_en", null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        do {
                            list.add(DeviceTypesRetrofitModel(it.getString(0)))
                        } while (it.moveToNext())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handleCorruption()
            }
            return list
        }
        return emptyList()
    }

    override suspend fun getBrands(type: String): List<DeviceBrandsRetrofitModel> {
        if (ensureDb()) {
            val list = mutableListOf<DeviceBrandsRetrofitModel>()
            try {
                val query = "SELECT DISTINCT b.name_en FROM brand b JOIN category c ON b.category_id = c.id WHERE c.name_en = ? AND b.name_en IS NOT NULL AND length(b.name_en) > 0 ORDER BY b.name_en"
                val cursor = sqliteDb?.rawQuery(query, arrayOf(type))
                cursor?.use {
                    if (it.moveToFirst()) {
                        do {
                            list.add(DeviceBrandsRetrofitModel(type = type, brand = it.getString(0)))
                        } while (it.moveToNext())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handleCorruption()
            }
            return list
        }
        return emptyList()
    }

    override suspend fun getCodes(
        type: String,
        brand: String,
        progress: MutableLiveData<Pair<Int, Int>?>?
    ): List<DeviceCodesRetrofitModel> {
        if (ensureDb()) {
            val resultList = mutableListOf<DeviceCodesRetrofitModel>()

            val query = """
                   SELECT ri.remote, ri.protocol, dr.key_name, dr.key_value, c.name_en, b.name_en 
                   FROM decode_remote dr 
                   JOIN remote_index ri ON dr.remote_index_id = ri.id
                   JOIN brand b ON ri.brand_id = b.id
                   JOIN category c ON ri.category_id = c.id
                   WHERE c.name_en = ? AND b.name_en = ? AND dr.key_name IS NOT NULL AND length(dr.key_name) > 0
                   ORDER BY ri.remote
               """.trimIndent()

            try {
                val cursor = sqliteDb?.rawQuery(query, arrayOf(type, brand))

                val remoteMap = mutableMapOf<String, MutableMap<String, String>>()

                cursor?.use {
                    val totalCount = it.count
                    progress?.postValue(Pair(0, totalCount))
                    var currentCount = 0

                    if (it.moveToFirst()) {
                        do {
                            currentCount++
                            if (currentCount % 10 == 0) {
                                progress?.postValue(Pair(currentCount, totalCount))
                            }

                            val remoteName = it.getString(0)
                            val protocol = it.getString(1)
                            val keyName = it.getString(2)
                            val keyValue = it.getString(3)
                            try {
                                if (!keyValue.isNullOrBlank()) {
                                    val timings = keyValue.split(",").mapNotNull { t -> t.trim().toIntOrNull() }
                                    if (timings.isNotEmpty()) {
                                        val freq = if (protocol.startsWith("48-NEC")) 48000 else 38000
                                        val hex = IrCsvParser.encodeToProntoHex(freq, timings)

                                        val keys = remoteMap.getOrPut(remoteName) { mutableMapOf() }
                                        keys[keyName] = hex
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } while (it.moveToNext())
                    }
                }
                progress?.postValue(null)


                remoteMap.forEach { (remoteName, codes) ->
                    if (codes.isNotEmpty()) {
                        resultList.add(
                            DeviceCodesRetrofitModel(
                                type = type,
                                brand = brand,
                                codes = codes
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handleCorruption()
            }
            return resultList
        }
        return emptyList()
    }
    
    fun close() {
        sqliteDb?.close()
    }

    private fun handleCorruption() {
        try {
            sqliteDb?.close()
        } catch (e: Exception) { e.printStackTrace() }
        sqliteDb = null
        
        val context = application
        val dirName = RepoDownloadManager.RepositoryInfo.IREXT.directoryName
        val repoDir = File(context.filesDir, "repos/$dirName")
        val dbFile = File(repoDir, "irext_db_20251031_sqlite3.db")
//        if (dbFile.exists()) {
//             dbFile.delete()
//        }
    }
}
