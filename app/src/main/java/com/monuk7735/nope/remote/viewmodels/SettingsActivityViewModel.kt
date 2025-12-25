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
    var darkModeSettingsValue: MutableLiveData<Int> = MutableLiveData<Int>()
    var dynamicColorSettingsValue: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    init {
        vibrateSettingsValue.value = settingsPreferences.getBoolean(
                application.getString(R.string.pref_settings_vibration),
                true
            )
        darkModeSettingsValue.value = settingsPreferences.getInt(
            application.getString(R.string.pref_settings_dark_mode),
            0 // Default to 0 (System)
        )
        dynamicColorSettingsValue.value = settingsPreferences.getBoolean(
            application.getString(R.string.pref_settings_dynamic_color),
            true
        )
    }

    fun saveSettings(context: Context) {
        settingsPreferences.edit()
            .putBoolean(
                context.getString(R.string.pref_settings_vibration),
                vibrateSettingsValue.value?:true
            )
            .putInt(
                context.getString(R.string.pref_settings_dark_mode),
                darkModeSettingsValue.value?:0
            )
            .putBoolean(
                context.getString(R.string.pref_settings_dynamic_color),
                dynamicColorSettingsValue.value?:true
            )
            .apply()
    }

}