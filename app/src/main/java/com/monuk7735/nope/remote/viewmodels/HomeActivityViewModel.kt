package com.monuk7735.nope.remote.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.ConsumerIrManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.monuk7735.nope.remote.database.RemoteDatabase
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.infrared.patterns.IRPatternDecoder
import com.monuk7735.nope.remote.models.database.MacroDataDBModel
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.repository.MacroDataRepository
import com.monuk7735.nope.remote.repository.RemoteDataRepository
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivityViewModel(application: Application) : AndroidViewModel(application) {

    val irController =
            IRController(
                    application.getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager
            )

    private val remoteDataRepository: RemoteDataRepository =
            RemoteDataRepository(remoteDao = RemoteDatabase.getDatabase(application).remoteDao())

    private val macroDataRepository: MacroDataRepository =
            MacroDataRepository(macroDao = RemoteDatabase.getDatabase(application).macroDao())

    val allMacros: LiveData<List<MacroDataDBModel>> = macroDataRepository.allMacros

    val allRemotesInfo: LiveData<List<RemoteDataDBModel>> = remoteDataRepository.allRemotesShortInfo

    fun deleteRemote(remote: RemoteDataDBModel) {
        viewModelScope.launch(Dispatchers.IO) { remoteDataRepository.deleteRemote(remote) }
    }

    init { }
}
