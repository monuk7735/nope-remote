package com.monuk7735.nope.remote.viewmodels

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.monuk7735.nope.remote.R
import com.monuk7735.nope.remote.service.DownloadState
import com.monuk7735.nope.remote.service.RepoDownloadManager
import com.monuk7735.nope.remote.service.RepoDownloadService
import com.monuk7735.nope.remote.service.RepoState
import java.io.File

class SettingsActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsPreferences: SharedPreferences =
        application.getSharedPreferences(
            application.getString(R.string.shared_pref_app_settings),
            MODE_PRIVATE,
        )

    var vibrateSettingsValue: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var darkModeSettingsValue: MutableLiveData<Int> = MutableLiveData<Int>()
    var dynamicColorSettingsValue: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var preferCustomUiGlobal: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var textButtonsOnlySettingsValue: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var androidTvVolumeKeysSettingsValue: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var androidTvDpadModeSettingsValue: MutableLiveData<Int> = MutableLiveData<Int>()

    init {
        vibrateSettingsValue.value =
            settingsPreferences.getBoolean(
                application.getString(R.string.pref_settings_vibration),
                true,
            )
        darkModeSettingsValue.value =
            settingsPreferences.getInt(
                application.getString(R.string.pref_settings_dark_mode),
                0,
            )
        dynamicColorSettingsValue.value =
            settingsPreferences.getBoolean(
                application.getString(R.string.pref_settings_dynamic_color),
                true,
            )
        preferCustomUiGlobal.value =
            settingsPreferences.getBoolean("pref_prefer_custom_ui_global", true)
        textButtonsOnlySettingsValue.value =
            settingsPreferences.getBoolean("pref_text_buttons_only", false)
        androidTvVolumeKeysSettingsValue.value =
            settingsPreferences.getBoolean("pref_android_tv_volume_keys", true)
        androidTvDpadModeSettingsValue.value =
            settingsPreferences.getInt("pref_android_tv_dpad_mode", 0)
    }

    fun clearCommandOutput() {
        RepoDownloadManager.clearLog()
    }

    val repoStates = RepoDownloadManager.repoStates
    val repoCommandOutput = RepoDownloadManager.commandOutput

    val availableRepositories = RepoDownloadManager.RepositoryInfo.values().toList()

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
            targetDir.walkTopDown().forEach { it.setWritable(true) }
            targetDir.deleteRecursively()
        }
        // settingsPreferences.edit().putBoolean("repo_installed_$directory", false).apply()

        RepoDownloadManager.updateState(
            directory,
            RepoState(DownloadState.IDLE),
        )
        RepoDownloadManager.log("Deleted repository: $directory")
    }

    fun isRepoInstalled(directory: String): Boolean {
        val context = getApplication<Application>()
        val reposDir = File(context.filesDir, "repos")
        val targetDir = File(reposDir, directory)

        if (!targetDir.exists()) return false

        val repoInfo = RepoDownloadManager.RepositoryInfo.fromDirectory(directory) ?: return false

        return if (repoInfo.mode == RepoDownloadManager.DownloadMode.DIRECT_FILE) {
            // Check for DB file
            targetDir.walk().any { it.extension == "db" || it.extension == "sqlite" }
        } else if (repoInfo.mode == RepoDownloadManager.DownloadMode.TAR_XZ) {
            // Check for Mi Remote dump structure
            val dumpDir = File(targetDir, "database_dump")
            val flatDump = File(targetDir, "1_TV") // In case it wasn't nested
            dumpDir.exists() || flatDump.exists()
        } else if (repoInfo == RepoDownloadManager.RepositoryInfo.FLIPPER_IRDB) {
            val flipperMainDir = File(targetDir, "Flipper-IRDB-main")
            val convertedDir = File(flipperMainDir, "_Converted_/Pronto")
            return flipperMainDir.exists() && convertedDir.exists()
        } else if (repoInfo == RepoDownloadManager.RepositoryInfo.FLIPPER_DEVICES_IRDB) {
            val flipperDevicesMainDir = File(targetDir, "IRDB-dev")
            val categoriesDir = File(flipperDevicesMainDir, "database/categories")
            return flipperDevicesMainDir.exists() && categoriesDir.exists()
        } else {
            // Check for index file (Probono)
            val indexFile = File(targetDir, "codes/index")
            val masterIndexFile = File(targetDir, "irdb-master/codes/index")
            indexFile.exists() || masterIndexFile.exists()
        }
    }

    fun saveSettings(context: Context) {
        settingsPreferences
            .edit()
            .putBoolean(
                context.getString(R.string.pref_settings_vibration),
                vibrateSettingsValue.value ?: true,
            )
            .putInt(
                context.getString(R.string.pref_settings_dark_mode),
                darkModeSettingsValue.value ?: 0,
            )
            .putBoolean(
                context.getString(R.string.pref_settings_dynamic_color),
                dynamicColorSettingsValue.value ?: true,
            )
            .putBoolean(
                "pref_prefer_custom_ui_global",
                preferCustomUiGlobal.value ?: true,
            )
            .putBoolean(
                "pref_text_buttons_only",
                textButtonsOnlySettingsValue.value ?: false,
            )
            .putBoolean(
                "pref_android_tv_volume_keys",
                androidTvVolumeKeysSettingsValue.value ?: true,
            )
            .putInt(
                "pref_android_tv_dpad_mode",
                androidTvDpadModeSettingsValue.value ?: 0,
            )
            .apply()
    }
}
