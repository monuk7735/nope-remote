package com.monuk7735.nope.remote.repository

import androidx.lifecycle.LiveData
import com.monuk7735.nope.remote.database.MacroDao
import com.monuk7735.nope.remote.models.database.MacroDataDBModel

class MacroDataRepository(
    private val macroDao: MacroDao
) {
    val allMacros: LiveData<List<MacroDataDBModel>> = macroDao.getAllMacros()

    fun getMacroByID(id: Int): LiveData<MacroDataDBModel> = macroDao.getMacroById(id)

    suspend fun addMacro(macroDataDBModel: MacroDataDBModel) {
        macroDao.addMacro(macroDataDBModel)
    }

    suspend fun deleteMacro(macroDataDBModel: MacroDataDBModel){
        macroDao.deleteMacro(macroDataDBModel)
    }

    suspend fun updateMacro(macroDataDBModel: MacroDataDBModel){
        macroDao.updateMacro(macroDataDBModel)
    }
}
