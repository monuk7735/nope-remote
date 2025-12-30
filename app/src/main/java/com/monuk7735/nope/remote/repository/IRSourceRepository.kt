package com.monuk7735.nope.remote.repository

import androidx.lifecycle.MutableLiveData
import com.monuk7735.nope.remote.models.retrofit.DeviceBrandsRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceCodesRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceTypesRetrofitModel

interface IRSourceRepository {
    fun isRepoInstalled(): Boolean
    suspend fun getTypes(): List<DeviceTypesRetrofitModel>
    suspend fun getBrands(type: String): List<DeviceBrandsRetrofitModel>
    suspend fun getCodes(
            type: String,
            brand: String,
            progress: MutableLiveData<Pair<Int, Int>?>? = null
    ): List<DeviceCodesRetrofitModel>
}
