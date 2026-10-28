package com.monuk7735.nope.remote.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.ConsumerIrManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.monuk7735.nope.remote.database.RemoteDatabase
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.models.database.MacroDataDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.models.database.SavedTvDeviceDBModel
import com.monuk7735.nope.remote.repository.MacroDataRepository
import com.monuk7735.nope.remote.repository.RemoteDataRepository
import com.monuk7735.nope.remote.repository.WifiRemoteRepository
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val consumerIrManager = try {
        application.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
    } catch (e: Exception) {
        null
    }

    val hasIrBlaster = consumerIrManager?.hasIrEmitter() == true

    val irController = if (hasIrBlaster) {
        IRController(consumerIrManager!!)
    } else {
        null
    }

    private val remoteDataRepository: RemoteDataRepository =
        RemoteDataRepository(remoteDao = RemoteDatabase.getDatabase(application).remoteDao())

    private val macroDataRepository: MacroDataRepository =
        MacroDataRepository(macroDao = RemoteDatabase.getDatabase(application).macroDao())

    private val wifiRemoteRepository: WifiRemoteRepository =
        WifiRemoteRepository(
            wifiRemoteDao = RemoteDatabase.getDatabase(application).wifiRemoteDao(),
        )

    val allMacros: LiveData<List<MacroDataDBModel>> = macroDataRepository.allMacros

    val allRemotesInfo: LiveData<List<RemoteDataDBModel>> = remoteDataRepository.allRemotesShortInfo

    val allWifiRemotes: LiveData<List<SavedTvDeviceDBModel>> = wifiRemoteRepository.allWifiRemotes

    fun deleteRemote(remote: RemoteDataDBModel) {
        viewModelScope.launch(Dispatchers.IO) { remoteDataRepository.deleteRemote(remote) }
    }

    fun saveWifiRemote(name: String, host: String, port: Int = 6466) {
        viewModelScope.launch(Dispatchers.IO) {
            wifiRemoteRepository.saveWifiRemote(name, host, port)
        }
    }

    fun deleteWifiRemote(tv: SavedTvDeviceDBModel) {
        viewModelScope.launch(Dispatchers.IO) {
            wifiRemoteRepository.deleteWifiRemote(tv)
        }
    }

    init { }
}
