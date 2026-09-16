package com.monuk7735.nope.remote.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.monuk7735.nope.remote.infrared.IrCsvParser
import com.monuk7735.nope.remote.infrared.patterns.*
import com.monuk7735.nope.remote.models.retrofit.DeviceBrandsRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceCodesRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceTypesRetrofitModel
import com.monuk7735.nope.remote.service.RepoDownloadManager
import java.io.File

class FlipperDevicesIRDBRepository(private val context: Context) : IRSourceRepository {

    private val repoDirName = RepoDownloadManager.RepositoryInfo.FLIPPER_DEVICES_IRDB.directoryName
    private val repoDir: File
        get() {
            val base = File(context.filesDir, "repos/$repoDirName")
            return File(base, "IRDB-dev/database/categories")
        }

    override fun isRepoInstalled(): Boolean {
        return repoDir.exists() && repoDir.isDirectory
    }

    override suspend fun getTypes(): List<DeviceTypesRetrofitModel> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (!isRepoInstalled()) return@withContext emptyList()

        repoDir.listFiles { file ->
            file.isDirectory && !file.name.startsWith(".")
        }?.map { dir ->
            val cleanName = dir.name.replace("_", " ")
            DeviceTypesRetrofitModel(cleanName)
        }?.sortedBy { it.type } ?: emptyList()
    }

    override suspend fun getBrands(type: String): List<DeviceBrandsRetrofitModel> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (!isRepoInstalled()) return@withContext emptyList()

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

        val targetTypeDir = repoDir.listFiles { file ->
            file.isDirectory && file.name.replace("_", " ") == type
        }?.firstOrNull() ?: return@withContext emptyList()

        val targetBrandDir = File(targetTypeDir, brand)
        if (!targetBrandDir.exists() || !targetBrandDir.isDirectory) {
            return@withContext emptyList()
        }

        val irFiles = mutableListOf<File>()
        targetBrandDir.listFiles { file -> file.isDirectory && !file.name.startsWith(".") }?.forEach { subDir ->
            subDir.listFiles { file -> !file.isDirectory && file.extension.equals("ir", ignoreCase = true) }?.forEach { irFile ->
                irFiles.add(irFile)
            }
        }
        targetBrandDir.listFiles { file -> !file.isDirectory && file.extension.equals("ir", ignoreCase = true) }?.forEach { irFile ->
            irFiles.add(irFile)
        }

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
                            codes = codeMap,
                            model = file.nameWithoutExtension
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
        var currentProtocol: String? = null
        var currentAddress: String? = null
        var currentCommand: String? = null
        
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#") || trimmed.isEmpty()) {
                if (currentName != null && currentType == "raw" && currentFrequency != null && currentData != null) {
                    processRawCommand(currentName, currentFrequency, currentData, result)
                } else if (currentName != null && currentType == "parsed" && currentProtocol != null && currentCommand != null) {
                    processParsedCommand(currentName, currentProtocol, currentAddress ?: "", currentCommand, result)
                }
                currentName = null
                currentType = null
                currentFrequency = null
                currentData = null
                currentProtocol = null
                currentAddress = null
                currentCommand = null
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
            } else if (trimmed.startsWith("protocol:")) {
                currentProtocol = trimmed.substringAfter("protocol:").trim()
            } else if (trimmed.startsWith("address:")) {
                currentAddress = trimmed.substringAfter("address:").trim()
            } else if (trimmed.startsWith("command:")) {
                currentCommand = trimmed.substringAfter("command:").trim()
            }
        }
        
        if (currentName != null && currentType == "raw" && currentFrequency != null && currentData != null) {
            processRawCommand(currentName, currentFrequency, currentData, result)
        } else if (currentName != null && currentType == "parsed" && currentProtocol != null && currentCommand != null) {
            processParsedCommand(currentName, currentProtocol, currentAddress ?: "", currentCommand, result)
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

    private fun processParsedCommand(
        name: String,
        protocol: String,
        addressHex: String,
        commandHex: String,
        result: MutableMap<String, String>
    ) {
        try {
            val addrBytes = addressHex.split("\\s+".toRegex()).mapNotNull { it.toIntOrNull(16) }
            val cmdBytes = commandHex.split("\\s+".toRegex()).mapNotNull { it.toIntOrNull(16) }
            val dev = addrBytes.getOrNull(0) ?: 0
            val subdev = addrBytes.getOrNull(1) ?: 0
            val func = cmdBytes.getOrNull(0) ?: 0

            val (generator: Protocol?, freq: Int) = when {
                protocol.equals("Samsung32", ignoreCase = true) -> Pair(NECSamsung(), 38000)
                protocol.equals("NEC", ignoreCase = true) -> Pair(NECStandard(), 38000)
                protocol.equals("NEC48", ignoreCase = true) -> Pair(NEC48k(), 48000)
                protocol.startsWith("SIRC", ignoreCase = true) || protocol.startsWith("Sony", ignoreCase = true) -> Pair(SonySIRC(12), 40000)
                protocol.equals("RC5", ignoreCase = true) -> Pair(RC5(), 36000)
                protocol.equals("Panasonic", ignoreCase = true) || protocol.equals("Kaseikyo", ignoreCase = true) -> Pair(Panasonic(), 36700)
                else -> Pair(null, 38000)
            }

            if (generator != null) {
                val timings = generator.generate(dev, subdev, func)
                val prontoHex = IrCsvParser.encodeToProntoHex(freq, timings)
                val cleanName = name.uppercase()
                result[cleanName] = prontoHex
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
