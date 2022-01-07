package com.monuk7735.nope.remote.models.retrofit

data class DeviceCodesRetrofitModel(
    val type: String,
    val brand: String,
    val codes: Map<String, String>,
)