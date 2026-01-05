package com.monuk7735.nope.remote.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.monuk7735.nope.remote.infrared.IrCsvParser
import com.monuk7735.nope.remote.models.retrofit.DeviceBrandsRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceCodesRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceTypesRetrofitModel
import com.monuk7735.nope.remote.service.RepoDownloadManager
import java.io.File

class ProbonopdRepository(private val application: Application) : IRSourceRepository {

    private var deviceIndex: Map<String, Map<String, List<String>>>? = null

    private fun ensureIndex(): Boolean {
        if (deviceIndex != null) return true

        val context = application
        // Use directory from Enum
        val dirName = RepoDownloadManager.RepositoryInfo.PROBONOPD.directoryName
        val repoDir = File(context.filesDir, "repos/$dirName")
        
        var indexFile = File(repoDir, "codes/index")
        var baseUrl = "file://${repoDir.absolutePath}/codes/"

        if (!indexFile.exists()) {
            val masterDir = File(repoDir, "irdb-master")
            if (masterDir.exists()) {
                indexFile = File(masterDir, "codes/index")
                baseUrl = "file://${masterDir.absolutePath}/codes/"
            }
        }

        if (indexFile.exists()) {
            try {
                val csv = indexFile.readText()
                parseIndex(csv, baseUrl)
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    private fun parseIndex(csv: String, baseUrl: String) {
        val lines = csv.lines()
        val tempIndex = mutableMapOf<String, MutableMap<String, MutableList<String>>>()

        lines.forEach { line ->
            val parts = line.split("/")
            if (parts.size >= 3) {
                val brand = parts[0]
                if (brand.isBlank()) return@forEach
                val type = parts[1]
                val path = baseUrl + line.trim().replace(" ", "%20")

                val typeMap = tempIndex.getOrPut(type) { mutableMapOf() }
                val brandList = typeMap.getOrPut(brand) { mutableListOf() }
                brandList.add(path)
            }
        }
        deviceIndex = tempIndex
    }

    override fun isRepoInstalled(): Boolean {
        return ensureIndex()
    }

    override suspend fun getTypes(): List<DeviceTypesRetrofitModel> {
        if (ensureIndex()) {
            return deviceIndex?.keys?.sorted()?.map { DeviceTypesRetrofitModel(it) } ?: emptyList()
        }
        return emptyList()
    }

    override suspend fun getBrands(type: String): List<DeviceBrandsRetrofitModel> {
        if (ensureIndex()) {
            return deviceIndex?.get(type)?.keys?.sorted()?.map { brand ->
                DeviceBrandsRetrofitModel(type = type, brand = brand)
            }
                    ?: emptyList()
        }
        return emptyList()
    }

    override suspend fun getCodes(
            type: String,
            brand: String,
            progress: MutableLiveData<Pair<Int, Int>?>?
    ): List<DeviceCodesRetrofitModel> {
        if (ensureIndex()) {
            val matchingFiles = deviceIndex?.get(type)?.get(brand) ?: emptyList()
            val totalRemoteFiles = matchingFiles.size
            progress?.postValue(Pair(0, totalRemoteFiles))

            var currentCount = 0
            val resultList = mutableListOf<DeviceCodesRetrofitModel>()

            for (path in matchingFiles) {
                currentCount++
                progress?.postValue(Pair(currentCount, totalRemoteFiles))
                try {
                    var csvContent: String? = null
                    if (path.startsWith("file://")) {
                        val filePath = path.removePrefix("file://").replace("%20", " ")
                        val file = File(filePath)
                        if (file.exists()) {
                            csvContent = file.readText()
                        }
                    }

                    if (csvContent != null) {
                        val codeMap = IrCsvParser.parseCsvAndGenerateHex(csvContent)
                            .filterKeys { it.isNotBlank() }
                            .mapKeys { it.key.uppercase() }
                        if (codeMap.isNotEmpty()) {
                            resultList.add(
                                    DeviceCodesRetrofitModel(
                                            type = type,
                                            brand = brand,
                                            codes = codeMap
                                    )
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            progress?.postValue(null)
            return resultList
        }
        return emptyList()
    }
}
