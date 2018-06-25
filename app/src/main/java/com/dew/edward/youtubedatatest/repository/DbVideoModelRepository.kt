package com.dew.edward.youtubedatatest.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.arch.paging.PagingRequestHelper
import android.content.Context
import android.support.annotation.MainThread
import android.util.Log
import com.dew.edward.youtubedatatest.api.YoutubeAPI
import com.dew.edward.youtubedatatest.database.VideoDb
import com.dew.edward.youtubedatatest.model.SearchVideoResponse
import com.dew.edward.youtubedatatest.model.VideoModel
import com.dew.edward.youtubedatatest.util.createStatusLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor
import java.util.concurrent.Executors


/**
 * Created by Edward on 6/24/2018.
 */
class DbVideoModelRepository(context: Context, useInMemory: Boolean): YoutubePostRepository {

    val db: VideoDb  by lazy {
        VideoDb.create(context, useInMemory)
    }
    val webService: YoutubeAPI = YoutubeAPI.create()
    val ioExecutor: Executor = Executors.newFixedThreadPool(5)

    private val pageStatus = PageStatus()
    val helper = PagingRequestHelper(ioExecutor)
    private val networkState = helper.createStatusLiveData()

    /**
     * Inserts the response into the database while also assigning position indices to items.
     */
    companion object {
        const val TAG = "DbVideoModelRepository"
        const val PAGEDLIST_PAGE_SIZE = 20

        fun insertResultIntoDb(db: VideoDb, title: String, items: List<VideoModel>) {

            db.runInTransaction {
                val start = db.videoDao().getNextIndexInVideo(title)
                val indexedItems = items.mapIndexed { index, child ->
                    child.indexInResponse = start + index
                    Log.d("insertResultIntoDb", "Index: $index")
                    child
                }
                db.videoDao().insert(indexedItems)
            }
        }

        fun createWebserviceCallback(
                db: VideoDb,
                query: String,
                helperRequestCallback: PagingRequestHelper.Request.Callback,
                ioExecutor: Executor,
                pageStatus: PageStatus) = object : Callback<SearchVideoResponse> {
            override fun onFailure(call: Call<SearchVideoResponse>?, t: Throwable?) {
                helperRequestCallback.recordFailure(t!!)
                Log.d(TAG, "networkState ERROR: ${t.message}")
            }

            override fun onResponse(call: Call<SearchVideoResponse>?, response: Response<SearchVideoResponse>?) {
                if (response != null && response.isSuccessful) {
                    ioExecutor.execute {
                        val data = response.body()
                        val mappedItems = data?.items?.map {
                            val video = VideoModel(it.snippet.title,
                                    it.snippet.publishedAt.extractDate(),
                                    it.snippet.thumbnails.high.url,
                                    it.id.videoId)
                            Log.d("ResponseData", "VideoModel: $video")
                            video
                        }

                        // update pageTokens
                        pageStatus.prevPage = data?.prevPageToken ?: ""
                        pageStatus.nextPage = data?.nextPageToken ?: ""
                        pageStatus.totalResults = data?.pageInfo?.totalResults ?: ""

                        db.runInTransaction {
                            db.videoDao().deleteVideosByQuery()
                            insertResultIntoDb(db, query, mappedItems!!)
                            val data = db.videoDao().dumpAll()
                            Log.d(TAG, " onResponse, after insertResultIntoDb, dumpAll: $data")
                        }
                        // since we are in bg thread now, post the result.
                        // help Request Callback will update the NetWorkState
                        helperRequestCallback.recordSuccess()
                    }
                } else {
                    Log.d(TAG, "onResponse Error: response = null or response isn't successful")
                }
            }
        }
    }


    /**
     * When refresh is called, we simply run a fresh network request and when it arrives, clear
     * the database table and insert all new items in a transaction.
     * <p>
     * Since the PagedList already uses a database bound data source, it will automatically be
     * updated after the database transaction is finished.
     */
    @MainThread
    private fun refresh(query: String): LiveData<NetworkState> {

        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            webService.searchVideo(query, pageStatus.nextPage).enqueue(
                    createWebserviceCallback(db, query, it, ioExecutor, pageStatus))
        }

        return networkState
    }

    /**
     * Returns a Listing for the given subreddit.
     */
    @MainThread
    override fun postsOfSearchYoutube(query: String, pageSize: Int): Listing<VideoModel> {
        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        Log.d(TAG, "postsOfSearchYoutube called, query =$query")
        val boundaryCallback = VideosBoundaryCallback(query)
        val dataSourceFactory = db.videoDao().getVideosByQuery("%$query%")

        val pagedList = LivePagedListBuilder(dataSourceFactory, PAGEDLIST_PAGE_SIZE)
                .setBoundaryCallback(boundaryCallback)


        Executors.newSingleThreadExecutor().execute {
            val dumpAll = db.videoDao().dumpAll()
            Log.d(TAG, "postsOfSearchYoutube $query DB stub: $dumpAll")

            val dumpQuery = db.videoDao().dumpByQuery("%$query%")
            Log.d(TAG, "postsOfSearchYoutube $query pagedList: $dumpQuery")
        }

        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
        // dispatched data in refreshTrigger
        // so this part code should locate in ViewModel, not here, and unit should be Query String
        val refreshTriger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTriger) { refresh(query) }

        return Listing(pagedList.build(), networkState,
                        retry = { helper.retryAllFailed() },
                        refresh = { refreshTriger.value = null },
                        refreshState = refreshState
        )
    }

    fun dumpDb(query: String){
//        db.videoDao().deleteVideosByQuery(query)
        val stub = db.videoDao().dumpAll()
        Log.d(TAG, "DB stub: $stub")
    }
    fun resetPageStatus() {
        pageStatus.prevPage = ""
        pageStatus.nextPage = ""
        pageStatus.totalResults = ""
    }

    inner class VideosBoundaryCallback(val query: String) : PagedList.BoundaryCallback<VideoModel>() {
        /**
         * Database returned 0 items. We should query the backend for more items.
         * initialize a new query
         */
        @MainThread
        override fun onZeroItemsLoaded() {
            Log.d(TAG, "onZeroItemsLoaded called:")
            //initialize a new query
//            resetPageStatus()
            // temporary for testing
            ioExecutor.execute { dumpDb(query) }

            helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
                webService.searchVideo(query).enqueue(
                        createWebserviceCallback(db, query, it, ioExecutor, pageStatus))
        }
        }

        /**
         * User reached to the end of the list.
         */
        override fun onItemAtEndLoaded(itemAtEnd: VideoModel) {
            Log.d(TAG, "onItemAtEndLoaded called:")
            helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
                webService.searchVideo(query, pageStatus.nextPage).enqueue(
                        createWebserviceCallback(db, query, it, ioExecutor, pageStatus))
            }
        }

        override fun onItemAtFrontLoaded(itemAtFront: VideoModel) {
            Log.d(TAG, "onItemAtFrontLoaded called:")
            // ignored, since we only ever append to what's in the DB
        }
    }
}