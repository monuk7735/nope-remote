package com.monuk7735.nope.remote.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel

@Dao
interface RemoteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addRemote(remoteDB: RemoteDataDBModel)

    @Query("SELECT * FROM remotes")
    fun getAllRemotes(): LiveData<List<RemoteDataDBModel>>

    @Query("SELECT * FROM remotes")
    fun getAllRemotesShortInfo(): LiveData<List<RemoteDataDBModel>>

    @Query("SELECT * FROM remotes WHERE id = :id")
    fun getLiveRemote(id: Int): LiveData<RemoteDataDBModel>

    @Update
    suspend fun updateRemote(remoteDB: RemoteDataDBModel)

    @Delete
    suspend fun deleteRemote(remoteDB: RemoteDataDBModel)
}
