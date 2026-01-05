package com.monuk7735.nope.remote.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.monuk7735.nope.remote.database.RemoteDatabase
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.models.retrofit.DeviceBrandsRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceCodesRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceTypesRetrofitModel
import com.monuk7735.nope.remote.repository.ProbonopdRepository
import com.monuk7735.nope.remote.repository.IrextRepository
import com.monuk7735.nope.remote.service.RepoDownloadManager
import com.monuk7735.nope.remote.repository.IRSourceRepository
import com.monuk7735.nope.remote.repository.RemoteDataRepository
import com.monuk7735.nope.remote.repository.MiRemoteDumpRepository
import kotlinx.coroutines.launch

class AddRemoteActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val remoteDataRepository: RemoteDataRepository =
            RemoteDataRepository(remoteDao = RemoteDatabase.getDatabase(application).remoteDao())

    private val allRepositories = listOf(
        Pair(RepoDownloadManager.RepositoryInfo.PROBONOPD.title, ProbonopdRepository(application)),
        Pair(RepoDownloadManager.RepositoryInfo.IREXT.title, IrextRepository(application)),
        Pair(RepoDownloadManager.RepositoryInfo.MI_REMOTE.title, MiRemoteDumpRepository(application))
    )

    private val installedRepoPairs = allRepositories.filter { it.second.isRepoInstalled() }.ifEmpty { listOf(allRepositories[0]) }

    private val repositories = installedRepoPairs.map { it.second }
    val availableRepos = installedRepoPairs.map { it.first }
    
    val selectedRepoIndex = MutableLiveData(0)

    private val currentRepo: IRSourceRepository
        get() = repositories[selectedRepoIndex.value ?: 0]

    val types: MutableLiveData<List<DeviceTypesRetrofitModel>> = MutableLiveData()
    val brands: MutableLiveData<List<DeviceBrandsRetrofitModel>?> = MutableLiveData()
    val codes: MutableLiveData<List<DeviceCodesRetrofitModel>?> = MutableLiveData()
    val loadingProgress: MutableLiveData<Pair<Int, Int>?> = MutableLiveData()

    private var allCachedTypes: List<DeviceTypesRetrofitModel> = emptyList()
    private var currentViewBrands: List<DeviceBrandsRetrofitModel>? = null

    init {
        getTypes()
    }

    fun selectRepository(index: Int) {
        if (index in repositories.indices && index != selectedRepoIndex.value) {
            selectedRepoIndex.value = index
            getTypes()
        }
    }

    private fun getTypes() {
        viewModelScope.launch {
            val typeList = currentRepo.getTypes()
            allCachedTypes = typeList
            types.value = typeList
        }
    }

    fun filterTypes(query: String) {
        if (query.isBlank()) {
            types.value = allCachedTypes
        } else {
            types.value = allCachedTypes.filter { it.type.contains(query, ignoreCase = true) }
        }
    }

    fun getBrands(type: String) {
        brands.value = null
        viewModelScope.launch {
            val brandList = currentRepo.getBrands(type)
            currentViewBrands = brandList
            brands.value = brandList
        }
    }

    fun filterBrands(query: String) {
        val list = currentViewBrands ?: return
        if (query.isBlank()) {
            brands.value = list
        } else {
            brands.value = list.filter { it.brand.contains(query, ignoreCase = true) }
        }
    }

    fun getCodes(type: String, brand: String) {
        codes.value = null
        viewModelScope.launch {
            val resultList = currentRepo.getCodes(type, brand, loadingProgress)
            codes.postValue(resultList)
        }
    }

    fun saveRemote(remoteDataDBModel: RemoteDataDBModel) {
        viewModelScope.launch { remoteDataRepository.addRemote(remoteDataDBModel) }
    }

    fun isRepoInstalled(): Boolean {
        return repositories.any { it.isRepoInstalled() }
    }
}
