package com.monuk7735.nope.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.monuk7735.nope.remote.backup.BackupRestoreManager
import com.monuk7735.nope.remote.backup.ConflictStrategy
import com.monuk7735.nope.remote.backup.ExportOptions
import com.monuk7735.nope.remote.backup.ImportOptions
import com.monuk7735.nope.remote.backup.NopeBackup
import com.monuk7735.nope.remote.backup.NopeBackupSerializer
import com.monuk7735.nope.remote.database.MacroDao
import com.monuk7735.nope.remote.database.RemoteDao
import com.monuk7735.nope.remote.database.WifiRemoteDao
import com.monuk7735.nope.remote.infrared.patterns.IRPattern
import com.monuk7735.nope.remote.infrared.patterns.IRPatternType
import com.monuk7735.nope.remote.models.custom.macros.MacroTransmit
import com.monuk7735.nope.remote.models.database.AndroidTvButtonDBModel
import com.monuk7735.nope.remote.models.database.MacroDataDBModel
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.models.database.SavedTvDeviceDBModel
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeRemoteDao : RemoteDao {
    val remotes = mutableListOf<RemoteDataDBModel>()
    private var nextId = 1

    override suspend fun addRemote(remoteDB: RemoteDataDBModel): Long {
        val idToUse = if (remoteDB.id == 0) nextId++ else remoteDB.id
        val remoteToStore = remoteDB.copy(id = idToUse)
        remotes.add(remoteToStore)
        return idToUse.toLong()
    }

    override fun getAllRemotes(): LiveData<List<RemoteDataDBModel>> = MutableLiveData(remotes)

    override suspend fun getAllRemotesList(): List<RemoteDataDBModel> = remotes.toList()

    override fun getAllRemotesShortInfo(): LiveData<List<RemoteDataDBModel>> =
        MutableLiveData(remotes)

    override fun getLiveRemote(id: Int): LiveData<RemoteDataDBModel> =
        MutableLiveData(remotes.firstOrNull { it.id == id })

    override suspend fun getRemote(id: Int): RemoteDataDBModel? =
        remotes.firstOrNull { it.id == id }

    override suspend fun updateRemote(remoteDB: RemoteDataDBModel) {
        val index = remotes.indexOfFirst { it.id == remoteDB.id }
        if (index >= 0) {
            remotes[index] = remoteDB
        }
    }

    override suspend fun deleteRemote(remoteDB: RemoteDataDBModel) {
        remotes.removeAll { it.id == remoteDB.id }
    }
}

class FakeWifiRemoteBackupDao : WifiRemoteDao {
    val remotes = mutableListOf<SavedTvDeviceDBModel>()
    private var nextId = 1

    override suspend fun insertOrUpdate(tv: SavedTvDeviceDBModel): Long {
        remotes.removeAll { it.host == tv.host }
        val idToUse = if (tv.id == 0) nextId++ else tv.id
        val stored = tv.copy(id = idToUse)
        remotes.add(stored)
        return idToUse.toLong()
    }

    override fun getAllWifiRemotes(): LiveData<List<SavedTvDeviceDBModel>> =
        MutableLiveData(remotes)

    override suspend fun getAllWifiRemotesList(): List<SavedTvDeviceDBModel> = remotes.toList()

    override fun getAllWifiRemotesFlow(): Flow<List<SavedTvDeviceDBModel>> = flowOf(remotes)

    override suspend fun getWifiRemoteByHost(host: String): SavedTvDeviceDBModel? =
        remotes.firstOrNull { it.host == host }

    override fun getWifiRemoteByHostFlow(host: String): Flow<SavedTvDeviceDBModel?> =
        flowOf(remotes.firstOrNull { it.host == host })

    override suspend fun updateWifiRemote(tv: SavedTvDeviceDBModel) {
        val index = remotes.indexOfFirst { it.id == tv.id }
        if (index >= 0) {
            remotes[index] = tv
        }
    }

    override suspend fun deleteWifiRemote(tv: SavedTvDeviceDBModel) {
        remotes.removeAll { it.host == tv.host }
    }

    override suspend fun deleteById(id: Int) {
        remotes.removeAll { it.id == id }
    }
}

class FakeMacroDao : MacroDao {
    val macros = mutableListOf<MacroDataDBModel>()
    private var nextId = 1

    override suspend fun addMacro(remote: MacroDataDBModel) {
        val idToUse = if (remote.id == 0) nextId++ else remote.id
        macros.add(remote.copy(id = idToUse))
    }

    override fun getAllMacros(): LiveData<List<MacroDataDBModel>> = MutableLiveData(macros)

    override suspend fun getAllMacrosList(): List<MacroDataDBModel> = macros.toList()

    override fun getMacroById(id: Int): LiveData<MacroDataDBModel> =
        MutableLiveData(macros.firstOrNull { it.id == id })

    override suspend fun deleteMacro(macroDataDBModel: MacroDataDBModel) {
        macros.removeAll { it.id == macroDataDBModel.id }
    }

    override suspend fun updateMacro(macroDataDBModel: MacroDataDBModel) {
        val index = macros.indexOfFirst { it.id == macroDataDBModel.id }
        if (index >= 0) {
            macros[index] = macroDataDBModel
        }
    }
}

class NopeBackupTest {

    private fun createSampleIrRemote(
        id: Int = 1,
        name: String = "Living Room TV",
    ): RemoteDataDBModel {
        val pattern = IRPattern(IRPatternType.Intervals, 38000, intArrayOf(9000, 4500, 560, 560))
        val onScreenButton = RemoteButtonDBModel(
            id = "btn-1",
            offsetX = 10f,
            offsetY = 20f,
            name = "POWER",
            irPattern = pattern,
            gridCol = 2,
            gridRow = 0,
        )
        return RemoteDataDBModel(
            id = id,
            name = name,
            brand = "Samsung",
            type = "TV",
            added = Date(1730000000000L),
            onScreenRemoteButtonDBS = listOf(onScreenButton),
            offScreenRemoteButtonDBS = emptyList(),
            preferCustomUi = true,
            model = "QLED",
            isCustom = false,
            gridHCount = 5,
            gridVCount = 10,
        )
    }

    private fun createSampleWifiRemote(
        id: Int = 1,
        host: String = "192.168.1.100",
    ): SavedTvDeviceDBModel {
        val button = AndroidTvButtonDBModel(
            id = "tv-btn-1",
            name = "POWER",
            keyCodeName = "KEYCODE_POWER",
            gridCol = 2,
            gridRow = 0,
            offsetX = 10f,
            offsetY = 10f,
        )
        return SavedTvDeviceDBModel(
            id = id,
            name = "Android TV",
            host = host,
            port = 6466,
            added = Date(1730000000000L),
            onScreenButtons = listOf(button),
            offScreenButtons = emptyList(),
            gridHCount = 5,
            gridVCount = 10,
        )
    }

    private fun createSampleMacro(id: Int = 1, sourceRemoteId: Int = 1): MacroDataDBModel {
        val pattern = IRPattern(IRPatternType.Intervals, 38000, intArrayOf(9000, 4500))
        val unit = MacroTransmit(
            name = "Power on",
            sourceRemoteId = sourceRemoteId,
            sourceButtonName = "POWER",
            irPattern = pattern,
        )
        return MacroDataDBModel(
            id = id,
            name = "Turn Everything On",
            macroUnits = listOf(unit),
        )
    }

    @Test
    fun testSerializationAndDeserialization() {
        val backup = NopeBackup(
            version = 1,
            appVersion = "2.6",
            exportDate = 1730000000000L,
            includesLayouts = true,
            irRemotes = listOf(createSampleIrRemote()),
            wifiRemotes = listOf(createSampleWifiRemote()),
            macros = listOf(createSampleMacro()),
        )

        val json = NopeBackupSerializer.serialize(backup)
        assertTrue(json.contains("\"version\": 1"))
        assertTrue(json.contains("\"appVersion\": \"2.6\""))
        assertTrue(json.contains("Living Room TV"))
        assertTrue(json.contains("Android TV"))
        assertTrue(json.contains("Turn Everything On"))

        val deserialized = NopeBackupSerializer.deserialize(json)
        assertEquals(1, deserialized.version)
        assertEquals("2.6", deserialized.appVersion)
        assertEquals(1730000000000L, deserialized.exportDate)
        assertTrue(deserialized.includesLayouts)
        assertEquals(1, deserialized.irRemotes?.size)
        assertEquals("Living Room TV", deserialized.irRemotes?.first()?.name)
        assertEquals(
            "POWER",
            deserialized.irRemotes?.first()?.onScreenRemoteButtonDBS?.first()?.name,
        )
        assertEquals(1, deserialized.wifiRemotes?.size)
        assertEquals("192.168.1.100", deserialized.wifiRemotes?.first()?.host)
        assertEquals(1, deserialized.macros?.size)
        assertEquals("Turn Everything On", deserialized.macros?.first()?.name)
    }

    @Test
    fun testStripLayoutFromRemote() {
        val original = createSampleIrRemote()
        val stripped = NopeBackupSerializer.stripLayoutFromRemote(original)

        val button = stripped.onScreenRemoteButtonDBS.first()
        assertEquals(0f, button.offsetX, 0.001f)
        assertEquals(0f, button.offsetY, 0.001f)
        assertNull(button.gridCol)
        assertNull(button.gridRow)
        assertEquals(5, stripped.gridHCount)
        assertEquals(10, stripped.gridVCount)
    }

    @Test
    fun testStripLayoutFromWifiRemote() {
        val original = createSampleWifiRemote()
        val stripped = NopeBackupSerializer.stripLayoutFromWifiRemote(original)

        assertNull(stripped.onScreenButtons)
        assertNull(stripped.offScreenButtons)
        assertEquals(5, stripped.gridHCount)
        assertEquals(10, stripped.gridVCount)
    }

    @Test
    fun testCreateBackupWithOptions() = runBlocking {
        val remoteDao = FakeRemoteDao()
        val wifiDao = FakeWifiRemoteBackupDao()
        val macroDao = FakeMacroDao()

        remoteDao.addRemote(createSampleIrRemote())
        wifiDao.insertOrUpdate(createSampleWifiRemote())
        macroDao.addMacro(createSampleMacro())

        val manager = BackupRestoreManager(remoteDao, wifiDao, macroDao)

        // Test with all included
        val fullBackup = manager.createBackup(
            ExportOptions(
                includeIrRemotes = true,
                includeWifiRemotes = true,
                includeMacros = true,
                includeLayouts = true,
            ),
            "2.6",
        )
        assertNotNull(fullBackup.irRemotes)
        assertNotNull(fullBackup.wifiRemotes)
        assertNotNull(fullBackup.macros)
        assertTrue(fullBackup.includesLayouts)

        // Test with partial selection
        val partialBackup = manager.createBackup(
            ExportOptions(
                includeIrRemotes = true,
                includeWifiRemotes = false,
                includeMacros = false,
                includeLayouts = false,
            ),
            "2.6",
        )
        assertNotNull(partialBackup.irRemotes)
        assertNull(partialBackup.wifiRemotes)
        assertNull(partialBackup.macros)
        assertFalse(partialBackup.includesLayouts)
        assertNull(partialBackup.irRemotes?.first()?.onScreenRemoteButtonDBS?.first()?.gridCol)
    }

    @Test
    fun testRestoreBackupOverwriteStrategy() = runBlocking {
        val remoteDao = FakeRemoteDao()
        val wifiDao = FakeWifiRemoteBackupDao()
        val macroDao = FakeMacroDao()

        // Pre-populate with existing data
        remoteDao.addRemote(createSampleIrRemote(id = 10, name = "Living Room TV"))
        wifiDao.insertOrUpdate(createSampleWifiRemote(id = 20, host = "192.168.1.100"))
        macroDao.addMacro(createSampleMacro(id = 30))

        val manager = BackupRestoreManager(remoteDao, wifiDao, macroDao)

        val backup = NopeBackup(
            version = 1,
            appVersion = "2.6",
            exportDate = System.currentTimeMillis(),
            includesLayouts = true,
            irRemotes = listOf(createSampleIrRemote(id = 1, name = "Living Room TV")),
            wifiRemotes = listOf(createSampleWifiRemote(id = 2, host = "192.168.1.100")),
            macros = listOf(createSampleMacro(id = 3)),
        )

        val result = manager.restoreBackup(
            backup,
            ImportOptions(
                restoreIrRemotes = true,
                restoreWifiRemotes = true,
                restoreMacros = true,
                restoreLayouts = true,
                conflictStrategy = ConflictStrategy.OVERWRITE,
            ),
        )

        assertEquals(1, result.irRestoredCount)
        assertEquals(1, result.wifiRestoredCount)
        assertEquals(1, result.macrosRestoredCount)

        // Ensure IDs were retained from existing items
        assertEquals(1, remoteDao.remotes.size)
        assertEquals(10, remoteDao.remotes.first().id)
        assertEquals(1, wifiDao.remotes.size)
        assertEquals(20, wifiDao.remotes.first().id)
        assertEquals(1, macroDao.macros.size)
        assertEquals(30, macroDao.macros.first().id)
    }

    @Test
    fun testRestoreBackupKeepBothStrategy() = runBlocking {
        val remoteDao = FakeRemoteDao()
        val wifiDao = FakeWifiRemoteBackupDao()
        val macroDao = FakeMacroDao()

        remoteDao.addRemote(createSampleIrRemote(id = 10, name = "Living Room TV"))
        macroDao.addMacro(createSampleMacro(id = 30))

        val manager = BackupRestoreManager(remoteDao, wifiDao, macroDao)

        val backup = NopeBackup(
            version = 1,
            appVersion = "2.6",
            exportDate = System.currentTimeMillis(),
            includesLayouts = true,
            irRemotes = listOf(createSampleIrRemote(id = 1, name = "Living Room TV")),
            macros = listOf(createSampleMacro(id = 3)),
        )

        val result = manager.restoreBackup(
            backup,
            ImportOptions(
                restoreIrRemotes = true,
                restoreWifiRemotes = false,
                restoreMacros = true,
                restoreLayouts = true,
                conflictStrategy = ConflictStrategy.KEEP_BOTH,
            ),
        )

        assertEquals(1, result.irRestoredCount)
        assertEquals(1, result.macrosRestoredCount)
        assertEquals(2, remoteDao.remotes.size)
        assertEquals(2, macroDao.macros.size)
    }

    @Test
    fun testRestoreBackupSkipStrategy() = runBlocking {
        val remoteDao = FakeRemoteDao()
        val wifiDao = FakeWifiRemoteBackupDao()
        val macroDao = FakeMacroDao()

        remoteDao.addRemote(createSampleIrRemote(id = 10, name = "Living Room TV"))
        wifiDao.insertOrUpdate(createSampleWifiRemote(id = 20, host = "192.168.1.100"))
        macroDao.addMacro(createSampleMacro(id = 30))

        val manager = BackupRestoreManager(remoteDao, wifiDao, macroDao)

        val backup = NopeBackup(
            version = 1,
            appVersion = "2.6",
            exportDate = System.currentTimeMillis(),
            includesLayouts = true,
            irRemotes = listOf(createSampleIrRemote(id = 1, name = "Living Room TV")),
            wifiRemotes = listOf(createSampleWifiRemote(id = 2, host = "192.168.1.100")),
            macros = listOf(createSampleMacro(id = 3)),
        )

        val result = manager.restoreBackup(
            backup,
            ImportOptions(
                restoreIrRemotes = true,
                restoreWifiRemotes = true,
                restoreMacros = true,
                restoreLayouts = true,
                conflictStrategy = ConflictStrategy.SKIP,
            ),
        )

        assertEquals(0, result.irRestoredCount)
        assertEquals(0, result.wifiRestoredCount)
        assertEquals(0, result.macrosRestoredCount)
        assertEquals(1, remoteDao.remotes.size)
        assertEquals(1, wifiDao.remotes.size)
        assertEquals(1, macroDao.macros.size)
    }

    @Test
    fun testMacroSourceRemoteIdRemappingOnRestore() = runBlocking {
        val remoteDao = FakeRemoteDao()
        val wifiDao = FakeWifiRemoteBackupDao()
        val macroDao = FakeMacroDao()

        val manager = BackupRestoreManager(remoteDao, wifiDao, macroDao)

        // Backup has remote id = 99 and macro pointing to sourceRemoteId = 99
        val backup = NopeBackup(
            version = 1,
            appVersion = "2.6",
            exportDate = System.currentTimeMillis(),
            includesLayouts = true,
            irRemotes = listOf(createSampleIrRemote(id = 99, name = "New TV")),
            macros = listOf(createSampleMacro(id = 5, sourceRemoteId = 99)),
        )

        manager.restoreBackup(
            backup,
            ImportOptions(
                restoreIrRemotes = true,
                restoreWifiRemotes = false,
                restoreMacros = true,
                restoreLayouts = true,
                conflictStrategy = ConflictStrategy.KEEP_BOTH,
            ),
        )

        val restoredRemote = remoteDao.remotes.first()
        val restoredMacro = macroDao.macros.first()

        assertEquals(restoredRemote.id, restoredMacro.macroUnits.first().sourceRemoteId)
    }
}
