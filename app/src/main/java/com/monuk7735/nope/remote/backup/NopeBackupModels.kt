package com.monuk7735.nope.remote.backup

import com.monuk7735.nope.remote.models.database.MacroDataDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.models.database.SavedTvDeviceDBModel

data class NopeBackup(
    val version: Int = CURRENT_BACKUP_VERSION,
    val appVersion: String,
    val exportDate: Long = System.currentTimeMillis(),
    val includesLayouts: Boolean = true,
    val irRemotes: List<RemoteDataDBModel>? = null,
    val wifiRemotes: List<SavedTvDeviceDBModel>? = null,
    val macros: List<MacroDataDBModel>? = null,
) {
    companion object {
        const val CURRENT_BACKUP_VERSION = 1
    }
}

enum class ConflictStrategy {
    OVERWRITE,
    KEEP_BOTH,
    SKIP,
}

data class ExportOptions(
    val includeIrRemotes: Boolean = true,
    val includeWifiRemotes: Boolean = true,
    val includeMacros: Boolean = true,
    val includeLayouts: Boolean = true,
)

data class ImportOptions(
    val restoreIrRemotes: Boolean = true,
    val restoreWifiRemotes: Boolean = true,
    val restoreMacros: Boolean = true,
    val restoreLayouts: Boolean = true,
    val conflictStrategy: ConflictStrategy = ConflictStrategy.OVERWRITE,
)

data class RestoreResult(
    val irRestoredCount: Int = 0,
    val wifiRestoredCount: Int = 0,
    val macrosRestoredCount: Int = 0,
)
