package com.dew.edward.youtubedatatest.util

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PagingRequestHelper
import com.dew.edward.youtubedatatest.repository.NetworkState


/**
 * Created by Edward on 6/24/2018.
 */
private fun getErrorMessage(report: PagingRequestHelper.StatusReport): String {
    return PagingRequestHelper.RequestType.values().mapNotNull {
        report.getErrorFor(it)?.message
    }.first()
}

fun PagingRequestHelper.createStatusLiveData(): LiveData<NetworkState> {
    val liveData = MutableLiveData<NetworkState>()
    addListener { report ->
        //when can also be used as a replacement for an if-else if chain. If no argument is supplied,
        // the branch conditions are simply boolean expressions,
        // and a branch is executed when its condition is true:
        when {
            report.hasRunning() -> liveData.postValue(NetworkState.LOADING)
            report.hasError() -> liveData.postValue(
                    NetworkState.error(getErrorMessage(report)))
            else -> liveData.postValue(NetworkState.LOADED)
        }
    }
    return liveData
}
