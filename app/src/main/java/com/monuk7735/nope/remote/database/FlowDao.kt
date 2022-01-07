package com.monuk7735.nope.remote.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.monuk7735.nope.remote.models.database.FlowDataDBModel

@Dao
interface FlowDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFlow(remote: FlowDataDBModel)

    @Query("SELECT * FROM flows")
    fun getAllFlows(): LiveData<List<FlowDataDBModel>>

    @Query("SELECT * FROM flows WHERE id=:id")
    fun getFlowById(id:Int): LiveData<FlowDataDBModel>

    @Delete
    suspend fun deleteFlow(flowDataDBModel: FlowDataDBModel)

    @Update
    suspend fun updateFlow(flowDataDBModel: FlowDataDBModel)

}