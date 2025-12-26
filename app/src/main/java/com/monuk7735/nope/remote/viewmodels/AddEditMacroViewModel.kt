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
import com.monuk7735.nope.remote.repository.MacroDataRepository
import com.monuk7735.nope.remote.repository.RemoteDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.monuk7735.nope.remote.models.custom.macros.MacroTransmit

class AddEditMacroViewModel(application: Application) : AndroidViewModel(application) {

    val irController =
        IRController(application.getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager)

    private val remoteDataRepository: RemoteDataRepository = RemoteDataRepository(
        remoteDao = RemoteDatabase.getDatabase(application).remoteDao()
    )

    private val macroDataRepository: MacroDataRepository = MacroDataRepository(
        macroDao = RemoteDatabase.getDatabase(application).macroDao()
    )

    val allRemotes: LiveData<List<RemoteDataDBModel>> = remoteDataRepository.allRemotes

    var macroName by mutableStateOf("")
    val macroUnits: SnapshotStateList<MacroTransmit> = mutableStateListOf()

    fun initialize(macro: MacroDataDBModel) {
        if (macroName.isEmpty() && macroUnits.isEmpty()) {
            macroName = macro.name
            macroUnits.clear()
            macroUnits.addAll(macro.macroUnits)
        }
    }

    fun addUnit(unit: MacroTransmit) {
        macroUnits.add(unit)
    }

    fun removeUnit(index: Int) {
        if (index in macroUnits.indices) {
            macroUnits.removeAt(index)
        }
    }

    fun updateUnit(index: Int, unit: MacroTransmit) {
        if (index in macroUnits.indices) {
            macroUnits[index] = unit
        }
    }

    fun reorderUnits(from: Int, to: Int) {
        if (from in macroUnits.indices && to in macroUnits.indices) {
            macroUnits.add(to, macroUnits.removeAt(from))
        }
    }

    fun getRemote(id: Int): RemoteDataDBModel? {
        allRemotes.value?.forEach {
            if (it.id == id)
                return it
        }
        return null
    }

    fun addMacro(macroDataDBModel: MacroDataDBModel) {
        viewModelScope.launch(Dispatchers.IO) {
            macroDataRepository.addMacro(macroDataDBModel)
        }
    }

    fun deleteMacro(macroDataDBModel: MacroDataDBModel) {
        viewModelScope.launch(Dispatchers.IO) {
            macroDataRepository.deleteMacro(macroDataDBModel)
        }
    }

    fun updateMacro(macroDataDBModel: MacroDataDBModel){
        viewModelScope.launch(Dispatchers.IO) {
            macroDataRepository.updateMacro(macroDataDBModel)
        }
    }

}
