package com.monuk7735.nope.remote.database

import androidx.lifecycle.LiveData
import androidx.room.*
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