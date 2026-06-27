package com.monuk7735.nope.remote.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.monuk7735.nope.remote.infrared.IrCsvParser
import com.monuk7735.nope.remote.models.retrofit.DeviceBrandsRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceCodesRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceTypesRetrofitModel
import com.monuk7735.nope.remote.service.RepoDownloadManager
import java.io.File

class FlipperIrdbRepository(private val context: Context) : IRSourceRepository {

    private val repoDirName = RepoDownloadManager.RepositoryInfo.FLIPPER_IRDB.directoryName
    private val repoDir: File
        get() {
            val base = File(context.filesDir, "repos/$repoDirName")
            return File(base, "Flipper-IRDB-main")
        }
        
    private val convertedProntoDir: File
        get() = File(repoDir, "_Converted_/Pronto")

    override fun isRepoInstalled(): Boolean {
        return repoDir.exists() && repoDir.isDirectory && convertedProntoDir.exists()
    }

    override suspend fun getTypes(): List<DeviceTypesRetrofitModel> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (!isRepoInstalled()) return@withContext emptyList()

        val ignoredDirs = setOf(".git", ".github", "_Converted_", "ACs") // ACs often have separate complex protocols not present in pronto
        
        repoDir.listFiles { file ->
            file.isDirectory && !file.name.startsWith(".") && !ignoredDirs.contains(file.name)
        }?.map { dir ->
            val cleanName = dir.name.replace("_", " ")
            DeviceTypesRetrofitModel(cleanName)
        }?.sortedBy { it.type } ?: emptyList()
    }

    override suspend fun getBrands(type: String): List<DeviceBrandsRetrofitModel> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (!isRepoInstalled()) return@withContext emptyList()

        // Find the original type directory which might have underscores
        val targetTypeDir = repoDir.listFiles { file ->
            file.isDirectory && file.name.replace("_", " ") == type
        }?.firstOrNull() ?: return@withContext emptyList()

        targetTypeDir.listFiles { file ->
            file.isDirectory && !file.name.startsWith(".")
        }?.map { dir ->
            DeviceBrandsRetrofitModel(type = type, brand = dir.name)
        }?.sortedBy { it.brand } ?: emptyList()
    }

    override suspend fun getCodes(
        type: String,
        brand: String,
        progress: MutableLiveData<Pair<Int, Int>?>?
    ): List<DeviceCodesRetrofitModel> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (!isRepoInstalled()) return@withContext emptyList()

        // Map brand to the Pronto converted directory.
        // E.g., brand "Samsung" is under `_Converted_/Pronto/S/Samsung`
        // 0-9 brands are under `_Converted_/Pronto/0-9/`
        val firstChar = brand.firstOrNull() ?: return@withContext emptyList()
        val letterDirName = if (firstChar.isDigit()) "0-9" else firstChar.uppercase()
        
        val targetProntoBrandDir = File(convertedProntoDir, "$letterDirName/$brand")
        if (!targetProntoBrandDir.exists() || !targetProntoBrandDir.isDirectory) {
            return@withContext emptyList()
        }

        val irFiles = targetProntoBrandDir.listFiles { file ->
            !file.isDirectory && file.extension.equals("ir", ignoreCase = true)
        }?.toList() ?: emptyList()

        if (irFiles.isEmpty()) return@withContext emptyList()

        val list = mutableListOf<DeviceCodesRetrofitModel>()
        val totalFiles = irFiles.size
        var currentProgress = 0

        irFiles.forEach { file ->
            currentProgress++
            progress?.postValue(Pair(currentProgress, totalFiles))
            try {
                val content = file.readText()
                val codeMap = parseRawFlipperIr(content)
                if (codeMap.isNotEmpty()) {
                    list.add(
                        DeviceCodesRetrofitModel(
                            type = type,
                            brand = brand,
                            codes = codeMap
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        progress?.postValue(null)
        list
    }

    private fun parseRawFlipperIr(content: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val lines = content.lines()
        
        var currentName: String? = null
        var currentType: String? = null
        var currentFrequency: Int? = null
        var currentData: String? = null
        
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#") || trimmed.isEmpty()) {
                // If we hit a block separator (#) or empty line, process the collected data
                if (currentName != null && currentType == "raw" && currentFrequency != null && currentData != null) {
                    processRawCommand(currentName, currentFrequency, currentData, result)
                }
                // Reset for next block
                currentName = null
                currentType = null
                currentFrequency = null
                currentData = null
                continue
            }
            
            if (trimmed.startsWith("name:")) {
                currentName = trimmed.substringAfter("name:").trim()
            } else if (trimmed.startsWith("type:")) {
                currentType = trimmed.substringAfter("type:").trim()
            } else if (trimmed.startsWith("frequency:")) {
                currentFrequency = trimmed.substringAfter("frequency:").trim().toIntOrNull()
            } else if (trimmed.startsWith("data:")) {
                currentData = trimmed.substringAfter("data:").trim()
            }
        }
        
        // Process the last block if file doesn't end with a blank line or #
        if (currentName != null && currentType == "raw" && currentFrequency != null && currentData != null) {
            processRawCommand(currentName, currentFrequency, currentData, result)
        }

        return result
    }
    
    private fun processRawCommand(name: String, freq: Int, data: String, result: MutableMap<String, String>) {
        try {
            val timings = data.split("\\s+".toRegex()).mapNotNull { it.toIntOrNull() }
            if (timings.isNotEmpty()) {
                val prontoHex = IrCsvParser.encodeToProntoHex(freq, timings)
                val cleanName = name.uppercase()
                result[cleanName] = prontoHex
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
