package com.monuk7735.nope.remote.repository

import androidx.lifecycle.LiveData
import com.monuk7735.nope.remote.database.FlowDao
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.models.database.FlowDataDBModel

class FlowDataRepository(
    private val flowDao: FlowDao
) {
    val allFlows: LiveData<List<FlowDataDBModel>> = flowDao.getAllFlows()

    fun getFlowByID(id: Int): LiveData<FlowDataDBModel> = flowDao.getFlowById(id)

    suspend fun addFlow(flowDataDBModel: FlowDataDBModel) {
        flowDao.addFlow(flowDataDBModel)
    }

    suspend fun deleteFlow(flowDataDBModel: FlowDataDBModel){
        flowDao.deleteFlow(flowDataDBModel)
    }

    suspend fun updateFlow(flowDataDBModel: FlowDataDBModel){
        flowDao.updateFlow(flowDataDBModel)
    }
}