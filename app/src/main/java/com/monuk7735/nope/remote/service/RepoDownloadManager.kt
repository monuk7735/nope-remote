package com.monuk7735.nope.remote.service

import androidx.lifecycle.MutableLiveData

object RepoDownloadManager {
    val downloadStatus: MutableLiveData<String> = MutableLiveData("")
    val downloadProgress: MutableLiveData<Float> = MutableLiveData(0f)
    val activeRepoId: MutableLiveData<String> = MutableLiveData("")
    val commandOutput: MutableLiveData<String> = MutableLiveData("")

    fun log(message: String) {
        val current = commandOutput.value ?: ""
        commandOutput.postValue("$current\n$message")
    }

    fun clear() {
        downloadStatus.postValue("")
        downloadProgress.postValue(0f)
        activeRepoId.postValue("")
        commandOutput.postValue("")
    }
}
