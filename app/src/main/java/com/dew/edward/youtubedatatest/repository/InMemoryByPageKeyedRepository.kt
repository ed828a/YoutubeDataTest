package com.dew.edward.youtubedatatest.repository

import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.support.annotation.MainThread
import com.dew.edward.youtubedatatest.api.YoutubeAPI
import com.dew.edward.youtubedatatest.model.VideoModel
import java.util.concurrent.Executor

class InMemoryByPageKeyedRepository(private val youtubeApi: YoutubeAPI,
                                    private val networkExecutor: Executor): YoutubePostRepository {

    @MainThread
    override fun postsOfSearchYoutube(searchYoutube: String, pageSize: Int): Listing<VideoModel> {
        val sourceFactory = YoutubeDataSourceFactory(youtubeApi, searchYoutube, networkExecutor )
        val livePagedList = LivePagedListBuilder(sourceFactory, pageSize)
                .setFetchExecutor(networkExecutor)
                .build()

        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it -> it.initialLoad
        }

        return Listing(
                pagedList = livePagedList,
                networkState =  Transformations.switchMap(sourceFactory.sourceLiveData) { it. networkState },
                retry = { sourceFactory.sourceLiveData.value?.retryAllFailed() },
                refresh = { sourceFactory.sourceLiveData.value?.retryAllFailed() },
                refreshState = refreshState
        )
    }
}