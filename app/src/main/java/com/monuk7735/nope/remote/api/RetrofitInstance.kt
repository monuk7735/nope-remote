package com.monuk7735.nope.remote.api

import com.monuk7735.nope.remote.api.Constants.Companion.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit



object RetrofitInstance {

    val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .build()
            chain.call()
            chain.proceed(request)
        }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()
    }

    val api: Database by lazy {
        retrofit.create(Database::class.java)
    }
}