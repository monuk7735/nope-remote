package com.monuk7735.nope.remote.repository

import androidx.lifecycle.LiveData
import com.monuk7735.nope.remote.database.WifiRemoteDao
import com.monuk7735.nope.remote.models.database.SavedTvDeviceDBModel
import kotlinx.coroutines.flow.Flow

class WifiRemoteRepository(private val wifiRemoteDao: WifiRemoteDao) {

    val allWifiRemotes: LiveData<List<SavedTvDeviceDBModel>> =
        wifiRemoteDao.getAllWifiRemotes()

    suspend fun getAllWifiRemotesList(): List<SavedTvDeviceDBModel> =
        wifiRemoteDao.getAllWifiRemotesList()

    suspend fun getWifiRemoteByHost(host: String): SavedTvDeviceDBModel? =
        wifiRemoteDao.getWifiRemoteByHost(host)

    suspend fun insertOrUpdate(tv: SavedTvDeviceDBModel): Long = wifiRemoteDao.insertOrUpdate(tv)

    fun getWifiRemoteByHostFlow(host: String): Flow<SavedTvDeviceDBModel?> =
        wifiRemoteDao.getWifiRemoteByHostFlow(host)

    suspend fun updateWifiRemote(tv: SavedTvDeviceDBModel) {
        wifiRemoteDao.updateWifiRemote(tv)
    }

    suspend fun saveWifiRemote(name: String, host: String, port: Int = 6466): Long {
        val existing = wifiRemoteDao.getWifiRemoteByHost(host)
        val tv = if (existing != null) {
            existing.copy(name = name, port = port)
        } else {
            SavedTvDeviceDBModel(name = name, host = host, port = port)
        }
        return wifiRemoteDao.insertOrUpdate(tv)
    }

    suspend fun deleteWifiRemote(tv: SavedTvDeviceDBModel) {
        wifiRemoteDao.deleteWifiRemote(tv)
    }
}
