package com.monuk7735.nope.remote.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.monuk7735.nope.remote.models.database.MacroDataDBModel

@Dao
interface MacroDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMacro(remote: MacroDataDBModel)

    @Query("SELECT * FROM macros")
    fun getAllMacros(): LiveData<List<MacroDataDBModel>>

    @Query("SELECT * FROM macros WHERE id=:id")
    fun getMacroById(id: Int): LiveData<MacroDataDBModel>

    @Delete
    suspend fun deleteMacro(macroDataDBModel: MacroDataDBModel)

    @Update
    suspend fun updateMacro(macroDataDBModel: MacroDataDBModel)
}
