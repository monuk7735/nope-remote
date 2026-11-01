package com.monuk7735.nope.remote.viewmodels

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.monuk7735.nope.remote.BuildConfig
import com.monuk7735.nope.remote.R
import com.monuk7735.nope.remote.backup.BackupRestoreManager
import com.monuk7735.nope.remote.backup.ExportOptions
import com.monuk7735.nope.remote.backup.ImportOptions
import com.monuk7735.nope.remote.backup.NopeBackup
import com.monuk7735.nope.remote.backup.NopeBackupSerializer
import com.monuk7735.nope.remote.database.RemoteDatabase
import com.monuk7735.nope.remote.models.database.MacroDataDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.models.database.SavedTvDeviceDBModel
import com.monuk7735.nope.remote.service.DownloadState
import com.monuk7735.nope.remote.service.RepoDownloadManager
import com.monuk7735.nope.remote.service.RepoDownloadService
import com.monuk7735.nope.remote.service.RepoState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SettingsActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsPreferences: SharedPreferences =
        application.getSharedPreferences(
            application.getString(R.string.shared_pref_app_settings),
            MODE_PRIVATE,
        )

    private val database = RemoteDatabase.getDatabase(application)
    private val backupRestoreManager = BackupRestoreManager(
        remoteDao = database.remoteDao(),
        wifiRemoteDao = database.wifiRemoteDao(),
        macroDao = database.macroDao(),
    )

    val irRemotes: LiveData<List<RemoteDataDBModel>> = database.remoteDao().getAllRemotes()
    val wifiRemotes: LiveData<List<SavedTvDeviceDBModel>> =
        database.wifiRemoteDao().getAllWifiRemotes()
    val macros: LiveData<List<MacroDataDBModel>> = database.macroDao().getAllMacros()

    var showExportDialog by mutableStateOf(false)
    var pendingExportOptions: ExportOptions? = null
    var pendingImportBackup: NopeBackup? by mutableStateOf(null)

    sealed class BackupEvent {
        data class Message(val text: String) : BackupEvent()
        data class Error(val text: String) : BackupEvent()
    }

    private val backupEventChannel = Channel<BackupEvent>(Channel.BUFFERED)
    val backupEvents = backupEventChannel.receiveAsFlow()

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

    fun generateBackupFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "noperemote_backup_$timestamp.json"
    }

    fun onExportOptionsConfirmed(options: ExportOptions) {
        pendingExportOptions = options
    }

    fun exportPendingBackupToUri(context: Context, uri: Uri) {
        val options = pendingExportOptions ?: ExportOptions()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val backup = backupRestoreManager.createBackup(options, BuildConfig.VERSION_NAME)
                val jsonString = NopeBackupSerializer.serialize(backup)
                BackupRestoreManager.writeBackupToUri(context, uri, jsonString)
                backupEventChannel.send(BackupEvent.Message("Backup exported successfully"))
            } catch (e: Exception) {
                backupEventChannel.send(
                    BackupEvent.Error("Failed to export backup: ${e.message ?: "Unknown error"}"),
                )
            } finally {
                pendingExportOptions = null
            }
        }
    }

    fun loadBackupFromUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonString = BackupRestoreManager.readBackupFromUri(context, uri)
                val backup = NopeBackupSerializer.deserialize(jsonString)
                pendingImportBackup = backup
            } catch (e: Exception) {
                backupEventChannel.send(
                    BackupEvent.Error("Invalid backup file: ${e.message ?: "Could not parse"}"),
                )
            }
        }
    }

    fun restorePendingBackup(options: ImportOptions) {
        val backup = pendingImportBackup ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = backupRestoreManager.restoreBackup(backup, options)
                val message = "Restored: ${result.irRestoredCount} IR remotes, " +
                    "${result.wifiRestoredCount} Android TV remotes, " +
                    "${result.macrosRestoredCount} macros"
                backupEventChannel.send(BackupEvent.Message(message))
            } catch (e: Exception) {
                backupEventChannel.send(
                    BackupEvent.Error("Failed to restore backup: ${e.message ?: "Unknown error"}"),
                )
            } finally {
                pendingImportBackup = null
            }
        }
    }

    fun cancelImport() {
        pendingImportBackup = null
    }
}
