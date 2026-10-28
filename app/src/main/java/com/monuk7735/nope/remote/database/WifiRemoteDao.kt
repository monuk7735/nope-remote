package com.monuk7735.nope.remote.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.monuk7735.nope.remote.models.database.SavedTvDeviceDBModel
import kotlinx.coroutines.flow.Flow

@Dao
interface WifiRemoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(tv: SavedTvDeviceDBModel): Long

    @Query("SELECT * FROM wifi_remotes ORDER BY added DESC")
    fun getAllWifiRemotes(): LiveData<List<SavedTvDeviceDBModel>>

    @Query("SELECT * FROM wifi_remotes ORDER BY added DESC")
    fun getAllWifiRemotesFlow(): Flow<List<SavedTvDeviceDBModel>>

    @Query("SELECT * FROM wifi_remotes WHERE host = :host LIMIT 1")
    suspend fun getWifiRemoteByHost(host: String): SavedTvDeviceDBModel?

    @Update
    suspend fun updateWifiRemote(tv: SavedTvDeviceDBModel)

    @Delete
    suspend fun deleteWifiRemote(tv: SavedTvDeviceDBModel)

    @Query("DELETE FROM wifi_remotes WHERE id = :id")
    suspend fun deleteById(id: Int)
}
