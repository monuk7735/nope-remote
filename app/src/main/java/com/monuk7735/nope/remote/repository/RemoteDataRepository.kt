package com.monuk7735.nope.remote.repository

import androidx.lifecycle.LiveData
import com.monuk7735.nope.remote.database.RemoteDao
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel

class RemoteDataRepository(
    private val remoteDao: RemoteDao
) {

    val allRemotes: LiveData<List<RemoteDataDBModel>> = remoteDao.getAllRemotes()

    val allRemotesShortInfo : LiveData<List<RemoteDataDBModel>> = remoteDao.getAllRemotesShortInfo()

    fun getLiveRemote(id: Int): LiveData<RemoteDataDBModel> = remoteDao.getLiveRemote(id)

    suspend fun addRemote(remoteDataDBModel: RemoteDataDBModel) {
        remoteDao.addRemote(remoteDataDBModel)
    }

    suspend fun updateRemote(remoteDataDBModel: RemoteDataDBModel){
        remoteDao.updateRemote(remoteDataDBModel)
    }

    suspend fun deleteRemote(remoteDataDBModel: RemoteDataDBModel){
        remoteDao.deleteRemote(remoteDataDBModel)
    }

}