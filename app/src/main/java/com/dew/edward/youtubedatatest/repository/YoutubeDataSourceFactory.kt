package com.dew.edward.youtubedatatest.repository

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import com.dew.edward.youtubedatatest.api.YoutubeAPI
import com.dew.edward.youtubedatatest.model.VideoModel
import java.util.concurrent.Executor

class YoutubeDataSourceFactory (
        private val youtubeApi: YoutubeAPI,
        private val searchQuery: String,
        private val retryExecutor: Executor): DataSource.Factory<String, VideoModel>()
{
    val sourceLiveData = MutableLiveData<PageKeyedYoutubeDataSource>()

    override fun create(): DataSource<String, VideoModel> {
        val source = PageKeyedYoutubeDataSource(youtubeApi, searchQuery, retryExecutor)
        sourceLiveData.postValue(source)
        return  source
    }

}