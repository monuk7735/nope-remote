package com.monuk7735.nope.remote.repository

import android.content.Context
import android.util.Base64
import androidx.lifecycle.MutableLiveData
import com.monuk7735.nope.remote.infrared.IrCsvParser
import com.monuk7735.nope.remote.models.retrofit.DeviceBrandsRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceCodesRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceTypesRetrofitModel
import com.monuk7735.nope.remote.service.RepoDownloadManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class MiRemoteDumpRepository(private val context: Context) : IRSourceRepository {

    private val repoDirName = RepoDownloadManager.RepositoryInfo.MI_REMOTE.directoryName
    private val repoDir: File
        get() {
            val base = File(context.filesDir, "repos/$repoDirName")
            val nested = File(base, "database_dump")
            return if (nested.exists() && nested.isDirectory) nested else base
        }

    override fun isRepoInstalled(): Boolean {
        return repoDir.exists() && repoDir.isDirectory && repoDir.listFiles()?.isNotEmpty() == true
    }

    override suspend fun getTypes(): List<DeviceTypesRetrofitModel> {
        if (!isRepoInstalled()) {
             return emptyList()
        }
        
        return repoDir.listFiles { file -> file.isDirectory }
            ?.map { dir ->
                // "1_TV" -> "TV", "10_Cable _ Satellite box" -> "Cable Satellite box"
            val name = dir.name.substringAfter("_")
                .replace("_", " ")
                .replace("\\s+".toRegex(), " ")
                .trim()
            DeviceTypesRetrofitModel(name)
            }
            ?.sortedBy { it.type }
            ?: emptyList()
    }

    override suspend fun getBrands(type: String): List<DeviceBrandsRetrofitModel> {
        if (!isRepoInstalled()) return emptyList()
        
        // Find directory matching type
        val typeDir = repoDir.listFiles { file -> 
             file.isDirectory && file.name.substringAfter("_").equals(type, ignoreCase = true)
        }?.firstOrNull() 
        
        if (typeDir == null) {
            return emptyList()
        }

        return typeDir.listFiles { file -> file.extension == "json" }
            ?.map { file ->
                val rawName = file.nameWithoutExtension
                // Remove numeric suffix: "Xiaomi_153" -> "Xiaomi"
                val cleanName = if (rawName.contains("_") && rawName.substringAfterLast("_").all { it.isDigit() }) {
                    rawName.substringBeforeLast("_")
                } else {
                    rawName
                }
                DeviceBrandsRetrofitModel(type, cleanName)
            }
            ?.distinctBy { it.brand }
            ?.sortedBy { it.brand }
            ?: emptyList()
    }

    override suspend fun getCodes(
        type: String,
        brand: String,
        progress: MutableLiveData<Pair<Int, Int>?>?
    ): List<DeviceCodesRetrofitModel> {
        if (!isRepoInstalled()) return emptyList()

        val typeDir = repoDir.listFiles { file -> 
            file.isDirectory && file.name.substringAfter("_").equals(type, ignoreCase = true) 
        }?.firstOrNull() ?: return emptyList()

        // Find all files matching the clean brand name
        val targetFiles = typeDir.listFiles { file ->
            if (file.extension != "json") return@listFiles false
            val rawName = file.nameWithoutExtension
            val cleanName = if (rawName.contains("_") && rawName.substringAfterLast("_").all { it.isDigit() }) {
                rawName.substringBeforeLast("_")
            } else {
                rawName
            }
            cleanName.equals(brand, ignoreCase = true)
        }?.toList() ?: emptyList()
        
        if (targetFiles.isEmpty()) return emptyList()

        val list = mutableListOf<DeviceCodesRetrofitModel>()
        
        var currentProgress = 0
        // We can't know total easily without opening all files, assume 10 per file for progress? 
        // Or just post indeterminate.
        // Let's iterate files.
        
        targetFiles.forEach { file ->
            try {
                val jsonContent = file.readText()
                val jsonObject = JSONObject(jsonContent)
                val dataObj = jsonObject.optJSONObject("data")
                val othersArray = dataObj?.optJSONArray("others")
                
                if (othersArray != null) {
                    val total = othersArray.length()
                    for (i in 0 until total) {
                        if (i % 5 == 0) progress?.postValue(Pair(currentProgress + i, total * targetFiles.size)) // rough progress
                        
                        val remoteObj = othersArray.getJSONObject(i)
                        val keysObj = remoteObj.optJSONObject("key")
                        val frequency = remoteObj.optInt("frequency", 38000)
                        // val id = remoteObj.optString("_id") 
                        
                        if (keysObj != null) {
                            val codeMap = mutableMapOf<String, String>()
                            val keysIterator = keysObj.keys()
                            while(keysIterator.hasNext()){
                                val keyName = keysIterator.next()
                                val value = keysObj.getString(keyName)
                                try {
                                    val timings = decrypt(value)
                                    if (timings.isNotEmpty()) {
                                        val hex = IrCsvParser.encodeToProntoHex(frequency, timings)
                                        codeMap[keyName] = hex
                                    }
                                } catch (e: Exception) {
                                    // ignore bad keys
                                }
                            }
                            
                            if (codeMap.isNotEmpty()) {
                                list.add(DeviceCodesRetrofitModel(
                                    type = type,
                                    brand = brand, 
                                    codes = codeMap
                                ))
                            }
                        }
                    }
                    currentProgress += total
                }
            } catch (e: Exception) {
                // Ignore file error
            }
        }

        progress?.postValue(null)
        return list
    }
    
    // Decryption Logic
    // Key: fd7e915003168929c1a9b0ec32a60788
    private val secretKeySpec = SecretKeySpec("fd7e915003168929c1a9b0ec32a60788".toByteArray(Charsets.UTF_8), "AES")

    private fun decrypt(base64Str: String): List<Int> {
        try {
            // 1. Base64 Decode
            val encryptedBytes = Base64.decode(base64Str, Base64.DEFAULT)

            // 2. AES ECB Decrypt
            val cipher = Cipher.getInstance("AES/ECB/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
            val decryptedBytes = cipher.doFinal(encryptedBytes)

            // 3. Gzip Decompress
            val gzipInputStream = GZIPInputStream(ByteArrayInputStream(decryptedBytes))
            
            val reader = BufferedReader(InputStreamReader(gzipInputStream))
            val jsonString = reader.readText()
            
            // 4. Parse JSON Array [100, 200, ...]
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<Int>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getInt(i))
            }
            return list

        } catch (e: Exception) {
            return emptyList()
        }
    }
}
