package com.monuk7735.nope.remote.models.retrofit

import androidx.compose.ui.graphics.vector.ImageVector
import com.monuk7735.nope.remote.ui.theme.icons.SetTopBox
import com.monuk7735.nope.remote.ui.theme.icons.TV

data class DeviceTypesRetrofitModel(
    val type: String,
) {
    fun getIcon(): ImageVector {
        return when (type) {
            "TV" -> TV
            "Set-Top Box" -> SetTopBox

            else -> SetTopBox
        }
    }
}