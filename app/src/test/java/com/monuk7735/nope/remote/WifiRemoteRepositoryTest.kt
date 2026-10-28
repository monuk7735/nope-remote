package com.monuk7735.nope.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.monuk7735.nope.remote.database.WifiRemoteDao
import com.monuk7735.nope.remote.models.database.SavedTvDeviceDBModel
import com.monuk7735.nope.remote.repository.WifiRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FakeWifiRemoteDao : WifiRemoteDao {
    private val remotes = mutableListOf<SavedTvDeviceDBModel>()
    private val liveData = MutableLiveData<List<SavedTvDeviceDBModel>>()

    override suspend fun insertOrUpdate(tv: SavedTvDeviceDBModel): Long {
        remotes.removeAll { it.host == tv.host }
        remotes.add(tv)
        return tv.id.toLong()
    }

    override fun getAllWifiRemotes(): LiveData<List<SavedTvDeviceDBModel>> = liveData

    override fun getAllWifiRemotesFlow(): Flow<List<SavedTvDeviceDBModel>> = flowOf(remotes)

    override suspend fun getWifiRemoteByHost(host: String): SavedTvDeviceDBModel? =
        remotes.firstOrNull { it.host == host }

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

class WifiRemoteRepositoryTest {

    @Test
    fun testSavedTvDeviceDBModelDefaults() {
        val model = SavedTvDeviceDBModel(
            name = "Living Room TV",
            host = "192.168.1.100",
        )
        assertEquals(0, model.id)
        assertEquals("Living Room TV", model.name)
        assertEquals("192.168.1.100", model.host)
        assertEquals(6466, model.port)
        assertNotNull(model.added)
    }

    @Test
    fun testSaveAndGetWifiRemote() = runBlocking {
        val fakeDao = FakeWifiRemoteDao()
        val repository = WifiRemoteRepository(fakeDao)

        repository.saveWifiRemote("Bedroom TV", "192.168.1.101")
        val saved = fakeDao.getWifiRemoteByHost("192.168.1.101")

        assertNotNull(saved)
        assertEquals("Bedroom TV", saved?.name)
        assertEquals("192.168.1.101", saved?.host)
    }

    @Test
    fun testUpdateExistingWifiRemoteByHost() = runBlocking {
        val fakeDao = FakeWifiRemoteDao()
        val repository = WifiRemoteRepository(fakeDao)

        repository.saveWifiRemote("Old TV Name", "192.168.1.105")
        repository.saveWifiRemote("Updated TV Name", "192.168.1.105")

        val saved = fakeDao.getWifiRemoteByHost("192.168.1.105")
        assertNotNull(saved)
        assertEquals("Updated TV Name", saved?.name)
    }

    @Test
    fun testDeleteWifiRemote() = runBlocking {
        val fakeDao = FakeWifiRemoteDao()
        val repository = WifiRemoteRepository(fakeDao)

        repository.saveWifiRemote("Guest Room TV", "192.168.1.200")
        val saved = fakeDao.getWifiRemoteByHost("192.168.1.200")
        assertNotNull(saved)

        repository.deleteWifiRemote(saved!!)
        val deleted = fakeDao.getWifiRemoteByHost("192.168.1.200")
        assertNull(deleted)
    }
}
