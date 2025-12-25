package com.monuk7735.nope.remote.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface Database {
    @GET("index")
    suspend fun getIndex(): Response<ResponseBody>

    @GET
    suspend fun getCsv(@Url url: String): Response<ResponseBody>
}