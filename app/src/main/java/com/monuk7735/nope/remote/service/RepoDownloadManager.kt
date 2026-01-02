package com.monuk7735.nope.remote.service

import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ConcurrentHashMap

enum class DownloadState {
    IDLE,
    DOWNLOADING,
    EXTRACTING,
    INSTALLED,
    ERROR
}

data class RepoState(
    val state: DownloadState = DownloadState.IDLE,
    val progress: Float = 0f,
    val message: String = "",
    val isIndeterminate: Boolean = false
)

object RepoDownloadManager {
    enum class DownloadMode {
        ZIP_ARCHIVE,
        DIRECT_FILE
    }

    enum class RepositoryInfo(
        val title: String,
        val directoryName: String,
        val url: String, // The actual download URL (ZIP or DB)
        val displayUrl: String, // The URL to open in browser
        val mode: DownloadMode
    ) {
        PROBONOPD(
            "IRDB by Probonopd (CSV)",
            "irdb_probonopd",
            "https://github.com/probonopd/irdb/archive/refs/heads/master.zip",
            "https://github.com/probonopd/irdb",
            DownloadMode.ZIP_ARCHIVE
        ),
        IREXT(
            "IRDB by IREXT (SQLite)",
            "irdb_irext",
            "https://opensource.irext.net/irext/database/-/raw/master/db/irext_db_20251031_sqlite3.db",
            "https://opensource.irext.net/irext/database",
            DownloadMode.DIRECT_FILE
        );

        companion object {
            fun fromDirectory(dir: String): RepositoryInfo? = values().find { it.directoryName == dir }
        }
    }

    private val _repoStates = java.util.concurrent.ConcurrentHashMap<String, RepoState>()
    val repoStates: androidx.lifecycle.MutableLiveData<Map<String, RepoState>> = androidx.lifecycle.MutableLiveData(emptyMap())
    
    // Command output log (global)
    val commandOutput: MutableLiveData<String> = MutableLiveData("")

    fun updateState(repoId: String, state: RepoState) {
        val currentMap = repoStates.value?.toMutableMap() ?: mutableMapOf()
        currentMap[repoId] = state
        repoStates.postValue(currentMap)
    }
    
    fun getState(repoId: String): RepoState {
        return repoStates.value?.get(repoId) ?: RepoState()
    }

    fun log(message: String) {
        android.util.Log.d("RepoDownloadManager", message)
        val current = commandOutput.value ?: ""
        commandOutput.postValue("$current\n$message")
    }

    fun clearLog() {
        commandOutput.postValue("")
    }
}
