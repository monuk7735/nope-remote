package com.monuk7735.nope.remote.api

import com.monuk7735.nope.remote.models.retrofit.DeviceBrandsRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceCodesRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceTypesRetrofitModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface Database {
    @GET("devices/types/")
    suspend fun getTypes(): Response<List<DeviceTypesRetrofitModel>>

    @GET("devices/types/{type}/brands")
    suspend fun getBrands(@Path("type") type: String): Response<List<DeviceBrandsRetrofitModel>>

    @GET("devices/types/{type}/brands/{brand}/codes")
    suspend fun getCodes(
        @Path("type") type: String,
        @Path("brand") brand: String,
    ) : Response<List<DeviceCodesRetrofitModel>>

}