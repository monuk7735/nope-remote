package com.monuk7735.nope.remote.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.monuk7735.nope.remote.database.RemoteDatabase
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.repository.RemoteDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RemoteControlViewModel(application: Application) : AndroidViewModel(application) {

    var staticRemote: RemoteDataDBModel? = null

    private val remoteDataRepository: RemoteDataRepository = RemoteDataRepository(
        remoteDao = RemoteDatabase.getDatabase(application).remoteDao()
    )

    fun getLiveRemote(id: Int): LiveData<RemoteDataDBModel> {
        return remoteDataRepository.getLiveRemote(id)
    }

    fun updateRemote(remoteDataDBModel: RemoteDataDBModel) {
        viewModelScope.launch(Dispatchers.IO) {
            remoteDataRepository.updateRemote(remoteDataDBModel)
        }
    }

    fun deleteRemote(remoteDataDBModel: RemoteDataDBModel) {
        viewModelScope.launch(Dispatchers.IO) {
            remoteDataRepository.deleteRemote(remoteDataDBModel)
        }
    }
}