package com.monuk7735.nope.remote.backup

import android.content.Context
import android.net.Uri
import com.monuk7735.nope.remote.database.MacroDao
import com.monuk7735.nope.remote.database.RemoteDao
import com.monuk7735.nope.remote.database.WifiRemoteDao
import java.io.IOException

class BackupRestoreManager(
    private val remoteDao: RemoteDao,
    private val wifiRemoteDao: WifiRemoteDao,
    private val macroDao: MacroDao,
) {

    suspend fun createBackup(options: ExportOptions, appVersion: String): NopeBackup {
        val irRemotes = if (options.includeIrRemotes) {
            val remotes = remoteDao.getAllRemotesList()
            if (options.includeLayouts) {
                remotes
            } else {
                remotes.map { NopeBackupSerializer.stripLayoutFromRemote(it) }
            }
        } else {
            null
        }

        val wifiRemotes = if (options.includeWifiRemotes) {
            val tvs = wifiRemoteDao.getAllWifiRemotesList()
            if (options.includeLayouts) {
                tvs
            } else {
                tvs.map { NopeBackupSerializer.stripLayoutFromWifiRemote(it) }
            }
        } else {
            null
        }

        val macros = if (options.includeMacros) {
            macroDao.getAllMacrosList()
        } else {
            null
        }

        return NopeBackup(
            version = NopeBackup.CURRENT_BACKUP_VERSION,
            appVersion = appVersion,
            exportDate = System.currentTimeMillis(),
            includesLayouts = options.includeLayouts,
            irRemotes = irRemotes,
            wifiRemotes = wifiRemotes,
            macros = macros,
        )
    }

    suspend fun restoreBackup(backup: NopeBackup, options: ImportOptions): RestoreResult {
        var irRestoredCount = 0
        var wifiRestoredCount = 0
        var macrosRestoredCount = 0
        val idMapping = mutableMapOf<Int, Int>()

        if (options.restoreIrRemotes && !backup.irRemotes.isNullOrEmpty()) {
            val existingRemotes = remoteDao.getAllRemotesList()

            for (imported in backup.irRemotes) {
                val preparedRemote = if (options.restoreLayouts) {
                    imported
                } else {
                    NopeBackupSerializer.stripLayoutFromRemote(imported)
                }

                val existing = existingRemotes.firstOrNull {
                    it.name.equals(preparedRemote.name, ignoreCase = true) &&
                        it.brand.equals(preparedRemote.brand, ignoreCase = true)
                }

                if (existing != null) {
                    when (options.conflictStrategy) {
                        ConflictStrategy.OVERWRITE -> {
                            val remoteToUpdate = if (options.restoreLayouts) {
                                preparedRemote.copy(id = existing.id)
                            } else {
                                preparedRemote.copy(
                                    id = existing.id,
                                    onScreenRemoteButtonDBS = existing.onScreenRemoteButtonDBS,
                                    offScreenRemoteButtonDBS = existing.offScreenRemoteButtonDBS,
                                    gridHCount = existing.gridHCount,
                                    gridVCount = existing.gridVCount,
                                )
                            }
                            remoteDao.updateRemote(remoteToUpdate)
                            idMapping[imported.id] = existing.id
                            irRestoredCount++
                        }
                        ConflictStrategy.KEEP_BOTH -> {
                            val newId = remoteDao.addRemote(preparedRemote.copy(id = 0))
                            idMapping[imported.id] = newId.toInt()
                            irRestoredCount++
                        }
                        ConflictStrategy.SKIP -> {
                            idMapping[imported.id] = existing.id
                        }
                    }
                } else {
                    val newId = remoteDao.addRemote(preparedRemote.copy(id = 0))
                    idMapping[imported.id] = newId.toInt()
                    irRestoredCount++
                }
            }
        }

        if (options.restoreWifiRemotes && !backup.wifiRemotes.isNullOrEmpty()) {
            for (imported in backup.wifiRemotes) {
                val preparedWifi = if (options.restoreLayouts) {
                    imported
                } else {
                    NopeBackupSerializer.stripLayoutFromWifiRemote(imported)
                }

                val existing = wifiRemoteDao.getWifiRemoteByHost(preparedWifi.host)

                if (existing != null) {
                    when (options.conflictStrategy) {
                        ConflictStrategy.OVERWRITE -> {
                            val wifiToUpdate = if (options.restoreLayouts) {
                                preparedWifi.copy(id = existing.id)
                            } else {
                                preparedWifi.copy(
                                    id = existing.id,
                                    onScreenButtons = existing.onScreenButtons,
                                    offScreenButtons = existing.offScreenButtons,
                                    gridHCount = existing.gridHCount,
                                    gridVCount = existing.gridVCount,
                                )
                            }
                            wifiRemoteDao.updateWifiRemote(wifiToUpdate)
                            wifiRestoredCount++
                        }
                        ConflictStrategy.KEEP_BOTH -> {
                            // Host is unique in wifi_remotes table, so update existing row
                            val wifiToUpdate = preparedWifi.copy(id = existing.id)
                            wifiRemoteDao.updateWifiRemote(wifiToUpdate)
                            wifiRestoredCount++
                        }
                        ConflictStrategy.SKIP -> {
                            // Do nothing
                        }
                    }
                } else {
                    wifiRemoteDao.insertOrUpdate(preparedWifi.copy(id = 0))
                    wifiRestoredCount++
                }
            }
        }

        if (options.restoreMacros && !backup.macros.isNullOrEmpty()) {
            val existingMacros = macroDao.getAllMacrosList()

            for (imported in backup.macros) {
                val remappedUnits = imported.macroUnits.map { unit ->
                    val mappedRemoteId = idMapping[unit.sourceRemoteId] ?: unit.sourceRemoteId
                    unit.copy(sourceRemoteId = mappedRemoteId)
                }
                val preparedMacro = imported.copy(macroUnits = remappedUnits)

                val existing = existingMacros.firstOrNull {
                    it.name.equals(preparedMacro.name, ignoreCase = true)
                }

                if (existing != null) {
                    when (options.conflictStrategy) {
                        ConflictStrategy.OVERWRITE -> {
                            macroDao.updateMacro(preparedMacro.copy(id = existing.id))
                            macrosRestoredCount++
                        }
                        ConflictStrategy.KEEP_BOTH -> {
                            macroDao.addMacro(preparedMacro.copy(id = 0))
                            macrosRestoredCount++
                        }
                        ConflictStrategy.SKIP -> {
                            // Do nothing
                        }
                    }
                } else {
                    macroDao.addMacro(preparedMacro.copy(id = 0))
                    macrosRestoredCount++
                }
            }
        }

        return RestoreResult(
            irRestoredCount = irRestoredCount,
            wifiRestoredCount = wifiRestoredCount,
            macrosRestoredCount = macrosRestoredCount,
        )
    }

    companion object {
        fun writeBackupToUri(context: Context, uri: Uri, json: String) {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                os.bufferedWriter().use { writer ->
                    writer.write(json)
                }
            } ?: throw IOException("Unable to open output stream for URI: $uri")
        }

        fun readBackupFromUri(context: Context, uri: Uri): String =
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { reader ->
                    reader.readText()
                }
            } ?: throw IOException("Unable to open input stream for URI: $uri")
    }
}
