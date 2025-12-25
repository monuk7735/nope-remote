package com.monuk7735.nope.remote.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.ConsumerIrManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.monuk7735.nope.remote.api.RetrofitInstance
import com.monuk7735.nope.remote.database.RemoteDatabase
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.infrared.IrCsvParser
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.models.retrofit.DeviceBrandsRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceCodesRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceTypesRetrofitModel
import com.monuk7735.nope.remote.repository.RemoteDataRepository
import kotlinx.coroutines.launch

class AddRemoteActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val remoteDataRepository: RemoteDataRepository = RemoteDataRepository(
        remoteDao = RemoteDatabase.getDatabase(application).remoteDao()
    )

    val types: MutableLiveData<List<DeviceTypesRetrofitModel>> =
        MutableLiveData<List<DeviceTypesRetrofitModel>>()
    val brands: MutableLiveData<List<DeviceBrandsRetrofitModel>> =
        MutableLiveData<List<DeviceBrandsRetrofitModel>>()
    val codes: MutableLiveData<List<DeviceCodesRetrofitModel>> =
        MutableLiveData<List<DeviceCodesRetrofitModel>>()


    init {
        getTypes()
    }

    // Type -> Brand -> List<Path>
    private var deviceIndex: Map<String, Map<String, List<String>>>? = null

    private suspend fun ensureIndex(): Boolean {
        if (deviceIndex != null) return true
        return try {
            val response = RetrofitInstance.api.getIndex()
            if (response.isSuccessful && response.body() != null) {
                val csv = response.body()!!.string()
                val lines = csv.lines()

                val tempIndex = mutableMapOf<String, MutableMap<String, MutableList<String>>>()

                lines.forEach { line ->
                    val parts = line.split("/")
                    // Expected: Brand/Type/File.csv
                    if (parts.size >= 3) {
                        val brand = parts[0]
                        val type = parts[1]
                        val path = line // Store full path

                        val typeMap = tempIndex.getOrPut(type) { mutableMapOf() }
                        val brandList = typeMap.getOrPut(brand) { mutableListOf() }
                        brandList.add(path)
                    }
                }
                deviceIndex = tempIndex
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getTypes() {
        viewModelScope.launch {
            if (ensureIndex()) {
                val typeList = deviceIndex?.keys?.sorted()?.map { DeviceTypesRetrofitModel(it) } ?: emptyList()
                types.value = typeList
            }
        }
    }

    fun filterTypes(query: String) {
        val allTypes = deviceIndex?.keys?.sorted() ?: return
        if (query.isBlank()) {
            types.value = allTypes.map { DeviceTypesRetrofitModel(it) }
        } else {
            types.value = allTypes.filter { it.contains(query, ignoreCase = true) }.map { DeviceTypesRetrofitModel(it) }
        }
    }

    fun getBrands(type: String) {
        brands.value = null
        viewModelScope.launch {
            if (ensureIndex()) {
                val brandList = deviceIndex?.get(type)?.keys?.sorted()?.map { brand ->
                     DeviceBrandsRetrofitModel(type = type, brand = brand)
                } ?: emptyList()
                currentViewBrands = brandList
                brands.value = brandList
            }
        }
    }

    private var currentViewBrands: List<DeviceBrandsRetrofitModel>? = null

    fun filterBrands(query: String) {
         val list = currentViewBrands ?: return
         if (query.isBlank()) {
             brands.value = list
         } else {
             brands.value = list.filter { it.brand.contains(query, ignoreCase = true) }
         }
    }

    fun getCodes(type: String, brand: String) {
        codes.value = null
        viewModelScope.launch {
            if (ensureIndex()) {
                val matchingFiles = deviceIndex?.get(type)?.get(brand) ?: emptyList()
                
                val resultList = mutableListOf<DeviceCodesRetrofitModel>()
                
                for (path in matchingFiles) {
                    try {
                        val response = RetrofitInstance.api.getCsv(path)
                        if (response.isSuccessful && response.body() != null) {
                            val csvContent = response.body()!!.string()
                            val codeMap = IrCsvParser.parseCsvAndGenerateHex(csvContent)
                            
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
                codes.value = resultList
            }
        }
    }

    fun saveRemote(remoteDataDBModel: RemoteDataDBModel){
        viewModelScope.launch {
            remoteDataRepository.addRemote(remoteDataDBModel)
        }
    }

}