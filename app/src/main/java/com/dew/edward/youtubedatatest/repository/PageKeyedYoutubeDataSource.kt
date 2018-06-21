package com.dew.edward.youtubedatatest.repository

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PageKeyedDataSource
import com.dew.edward.youtubedatatest.api.YoutubeAPI
import com.dew.edward.youtubedatatest.model.SearchVideoResponse
import com.dew.edward.youtubedatatest.model.VideoModel
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.Executor

class PageKeyedYoutubeDataSource(
        private val youtubeApi: YoutubeAPI,
        private val searchQuery: String,
        private val retryExecutor: Executor): PageKeyedDataSource<String, VideoModel>() {

    // keep a function reference for the retry event
    private var retry: (() -> Any)? = null

    val networkState = MutableLiveData<NetworkState>()
    val initialLoad = MutableLiveData<NetworkState>()

    var prevPage = ""
    var nextPage = ""
    var totalResults = ""

    fun retryAllFailed(){
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            retryExecutor.execute {
                it.invoke()
            }
        }
    }



    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, VideoModel>) {
        val request = youtubeApi.searchVideo(query = searchQuery)
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        // triggered by a refresh, we better execute sync
        try {
            val response = request.execute()
            val data = response.body()
            val items = data?.items?.map{
                VideoModel(it.snippet.title, it.snippet.publishedAt.extractDate(), it.snippet.thumbnails.high.url, it.id.videoId)
            }

            prevPage = data?.prevPageToken ?: ""
            nextPage = data?.nextPageToken ?: ""
            totalResults = data?.pageInfo?.totalResults ?: ""

            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)
            callback.onResult(items!!.toMutableList(), prevPage, nextPage)
        } catch (ioException: IOException){
            retry = {
                loadInitial(params, callback)
            }
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, VideoModel>) {
        networkState.postValue(NetworkState.LOADING)
        youtubeApi.searchVideo(query = searchQuery).enqueue(object : retrofit2.Callback<SearchVideoResponse>{
            override fun onFailure(call: Call<SearchVideoResponse>?, t: Throwable?) {
                retry = {
                    loadAfter(params, callback)
                }
                networkState.postValue(NetworkState.error(t?.message ?: "unknown error"))
            }

            override fun onResponse(call: Call<SearchVideoResponse>?, response: Response<SearchVideoResponse>?) {
                if (response != null && response.isSuccessful){
                    val data = response.body()
                    val items = data?.items?.map {
                        VideoModel(it.snippet.title, it.snippet.publishedAt.extractDate(), it.snippet.thumbnails.high.url, it.id.videoId)
                    }
                    prevPage = data?.prevPageToken ?: ""
                    nextPage = data?.nextPageToken ?: ""
                    totalResults = data?.pageInfo?.totalResults ?: ""

                    retry = null
                    callback.onResult(items as MutableList<VideoModel>, nextPage)
                } else {
                    retry = {
                        loadAfter(params, callback)
                    }
                }
            }

        })
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, VideoModel>) {
        // ignored, since we only every append to our initial load.
    }
}