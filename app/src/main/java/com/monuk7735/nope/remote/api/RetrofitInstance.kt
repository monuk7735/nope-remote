package com.monuk7735.nope.remote.api

import com.monuk7735.nope.remote.api.Constants.Companion.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .build()
            chain.call()
            chain.proceed(request)
        }


//        .addInterceptor {
//            @Override
//            public Response intercept(Chain chain) throws IOException {
//                Request request = chain . request ().newBuilder().addHeader("parameter", "value")
//                    .build();
//                return chain.proceed(request);
//            }
//        };

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: Database by lazy {
        retrofit.create(Database::class.java)
    }
}