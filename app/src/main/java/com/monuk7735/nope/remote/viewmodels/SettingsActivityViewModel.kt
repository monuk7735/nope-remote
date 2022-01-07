package com.monuk7735.nope.remote.viewmodels

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.monuk7735.nope.remote.R

class SettingsActivityViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val settingsPreferences: SharedPreferences = application.getSharedPreferences(
        application.getString(R.string.shared_pref_app_settings),
        MODE_PRIVATE
    )

    var vibrateSettingsValue: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    init {
        vibrateSettingsValue.value = settingsPreferences.getBoolean(
                application.getString(R.string.pref_settings_vibration),
                true
            )
    }

    fun saveSettings(context: Context) {
        settingsPreferences.edit()
            .putBoolean(
                context.getString(R.string.pref_settings_vibration),
                vibrateSettingsValue.value?:true
            )
            .apply()

    }

}