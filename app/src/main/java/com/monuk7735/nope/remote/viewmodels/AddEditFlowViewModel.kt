package com.monuk7735.nope.remote.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.ConsumerIrManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.monuk7735.nope.remote.database.RemoteDatabase
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.models.database.FlowDataDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.repository.FlowDataRepository
import com.monuk7735.nope.remote.repository.RemoteDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddEditFlowViewModel(application: Application) : AndroidViewModel(application) {

    val irController =
        IRController(application.getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager)

    private val remoteDataRepository: RemoteDataRepository = RemoteDataRepository(
        remoteDao = RemoteDatabase.getDatabase(application).remoteDao()
    )

    private val flowDataRepository: FlowDataRepository = FlowDataRepository(
        flowDao = RemoteDatabase.getDatabase(application).flowDao()
    )

    val allRemotes: LiveData<List<RemoteDataDBModel>> = remoteDataRepository.allRemotes

    fun getRemote(id: Int): RemoteDataDBModel? {
        allRemotes.value?.forEach {
            if (it.id == id)
                return it
        }
        return null
    }

    fun addFlow(flowDataDBModel: FlowDataDBModel) {
        viewModelScope.launch(Dispatchers.IO) {
            flowDataRepository.addFlow(flowDataDBModel)
        }
    }

    fun deleteFlow(flowDataDBModel: FlowDataDBModel) {
        viewModelScope.launch(Dispatchers.IO) {
            flowDataRepository.deleteFlow(flowDataDBModel)
        }
    }

    fun updateFlow(flowDataDBModel: FlowDataDBModel){
        viewModelScope.launch(Dispatchers.IO) {
            flowDataRepository.updateFlow(flowDataDBModel)
        }
    }

//    val remotesInfoRepository = RemotesInfoRepository(sharedPreferences)

//    fun getAllRemotesInfo(): ArrayList<RemoteDataModel> {
//        return remotesInfoRepository.allRemotesData
//    }

//    fun getRemoteModel(uid: String): RemoteModel {
//        return RemoteDataRepository(
//            irController = IRController(
//                consumerIrManager = consumerIrManager
//            ),
//            sharedPreferences = sharedPreferences,
//            remoteUID = uid
//        ).remoteModel
//    }

}