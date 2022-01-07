package com.monuk7735.nope.remote.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.ConsumerIrManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.monuk7735.nope.remote.api.RetrofitInstance
import com.monuk7735.nope.remote.database.RemoteDatabase
import com.monuk7735.nope.remote.infrared.IRController
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

    fun getTypes() {
        viewModelScope.launch {
            val response = RetrofitInstance.api.getTypes()
            Log.d("monumonu", "getTypes: WasSuccessful? ${response.isSuccessful}")
            if (response.isSuccessful && response.body() != null)
                types.value = response.body()
        }
    }

    fun getBrands(type: String) {
        brands.value = null
        viewModelScope.launch {
            val response = RetrofitInstance.api.getBrands(type)
            if (response.isSuccessful && response.body() != null)
                brands.value = response.body()
        }
    }

    fun getCodes(type: String, brand: String) {
        codes.value = null
        viewModelScope.launch {
            val response = RetrofitInstance.api.getCodes(type, brand)
            Log.d("monumonu", "getCodes: ${response.isSuccessful}")
            if (response.isSuccessful && response.body() != null)
                codes.value = response.body()
        }
    }

    fun saveRemote(remoteDataDBModel: RemoteDataDBModel){
        viewModelScope.launch {
            remoteDataRepository.addRemote(remoteDataDBModel)
        }
    }

}