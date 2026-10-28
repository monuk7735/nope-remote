package com.monuk7735.nope.remote.repository

import androidx.lifecycle.LiveData
import com.monuk7735.nope.remote.database.WifiRemoteDao
import com.monuk7735.nope.remote.models.database.SavedTvDeviceDBModel

class WifiRemoteRepository(private val wifiRemoteDao: WifiRemoteDao) {

    val allWifiRemotes: LiveData<List<SavedTvDeviceDBModel>> =
        wifiRemoteDao.getAllWifiRemotes()

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
