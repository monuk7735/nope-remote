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
import com.monuk7735.nope.remote.utils.HomeSectionItem
import com.monuk7735.nope.remote.utils.HomeSectionsManager
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _homeSections =
        MutableStateFlow(HomeSectionsManager.getSections(application))
    val homeSections: StateFlow<List<HomeSectionItem>> = _homeSections.asStateFlow()

    fun refreshSections() {
        _homeSections.value = HomeSectionsManager.getSections(getApplication())
    }

    fun updateSections(newSections: List<HomeSectionItem>) {
        _homeSections.value = newSections
        HomeSectionsManager.saveSections(getApplication(), newSections)
    }

    fun resetSections() {
        _homeSections.value = HomeSectionsManager.resetToDefault(getApplication())
    }

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
