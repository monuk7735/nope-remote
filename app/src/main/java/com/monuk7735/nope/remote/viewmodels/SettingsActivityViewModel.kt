package com.monuk7735.nope.remote.viewmodels

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.monuk7735.nope.remote.R
import com.monuk7735.nope.remote.service.RepoDownloadManager
import com.monuk7735.nope.remote.service.RepoDownloadService
import android.content.Intent
import android.os.Build
import java.io.File

class SettingsActivityViewModel(
        application: Application,
) : AndroidViewModel(application) {

    private val settingsPreferences: SharedPreferences =
            application.getSharedPreferences(
                    application.getString(R.string.shared_pref_app_settings),
                    MODE_PRIVATE
            )

    var vibrateSettingsValue: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var darkModeSettingsValue: MutableLiveData<Int> = MutableLiveData<Int>()
    var dynamicColorSettingsValue: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    init {
        vibrateSettingsValue.value =
                settingsPreferences.getBoolean(
                        application.getString(R.string.pref_settings_vibration),
                        true
                )
        darkModeSettingsValue.value =
                settingsPreferences.getInt(
                        application.getString(R.string.pref_settings_dark_mode),
                        0
                )
        dynamicColorSettingsValue.value =
                settingsPreferences.getBoolean(
                        application.getString(R.string.pref_settings_dynamic_color),
                        true
                )
    }


    val repoDownloadStatus = RepoDownloadManager.downloadStatus
    val repoDownloadProgress = RepoDownloadManager.downloadProgress
    val activeRepoId = RepoDownloadManager.activeRepoId
    val repoCommandOutput = RepoDownloadManager.commandOutput

    data class RepoDef(val name: String, val url: String, val directory: String)

    val availableRepositories =
            listOf(
                RepoDef("CSV Repository", "https://github.com/probonopd/irdb.git", "irdb_official"),
                RepoDef("SQLite Repository", "https://opensource.irext.net/irext/database.git", "irext_sqlite")
            )

        fun manageRepository(url: String, name: String, directory: String) {
        val intent = Intent(getApplication(), RepoDownloadService::class.java).apply {
            action = RepoDownloadService.ACTION_START_DOWNLOAD
            putExtra(RepoDownloadService.EXTRA_URL, url)
            putExtra(RepoDownloadService.EXTRA_NAME, name)
            putExtra(RepoDownloadService.EXTRA_DIRECTORY, directory)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication<Application>().startForegroundService(intent)
        } else {
            getApplication<Application>().startService(intent)
        }
    }

    fun deleteRepository(directory: String) {
        val reposDir = File(getApplication<Application>().filesDir, "repos")
        val targetDir = File(reposDir, directory)
        if (targetDir.exists()) {
            targetDir.deleteRecursively()
        }
        settingsPreferences.edit().putBoolean("repo_installed_$directory", false).apply()
        

        RepoDownloadManager.downloadStatus.postValue("Deleted $directory")
        RepoDownloadManager.log("Deleted repository: $directory")
    }



    fun isRepoInstalled(directory: String): Boolean {
        return settingsPreferences.getBoolean("repo_installed_$directory", false)
    }

    fun saveSettings(context: Context) {
        settingsPreferences
                .edit()
                .putBoolean(
                        context.getString(R.string.pref_settings_vibration),
                        vibrateSettingsValue.value ?: true
                )
                .putInt(
                        context.getString(R.string.pref_settings_dark_mode),
                        darkModeSettingsValue.value ?: 0
                )
                .putBoolean(
                        context.getString(R.string.pref_settings_dynamic_color),
                        dynamicColorSettingsValue.value ?: true
                )
                .apply()
    }
}
