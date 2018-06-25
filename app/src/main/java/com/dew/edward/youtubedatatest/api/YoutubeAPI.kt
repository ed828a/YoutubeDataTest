package com.dew.edward.youtubedatatest.api

import android.util.Log
import com.dew.edward.youtubedatatest.model.SearchVideoResponse
import com.dew.edward.youtubedatatest.model.VideoModel
import com.dew.edward.youtubedatatest.modules.API_KEY
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface YoutubeAPI {

    @GET("search")
    fun searchVideo(@Query("q") query: String = "",
                    @Query("pageToken") pageToken: String = "",
                    @Query("part") part: String = "snippet",
                    @Query("maxResults") maxResults: String = "$NETWORK_PAGE_SIZE",
                    @Query("type") type: String = "video",
                    @Query("key") key: String = API_KEY): Call<SearchVideoResponse>

    @GET("search")
    fun getRelatedVideos(@Query("relatedToVideoId") relatedToVideoId: String = "",
                         @Query("pageToken") pageToken: String = "",
                         @Query("part") part: String = "snippet",
                         @Query("maxResults") maxResults: String = "$NETWORK_PAGE_SIZE",
                         @Query("type") type: String = "video",
                         @Query("key") key: String = API_KEY): Call<SearchVideoResponse>

    companion object {
        private const val YOUTUBE_BASE_URL = "https://www.googleapis.com/youtube/v3/"
        private const val NETWORK_PAGE_SIZE = 50  //should be 50 in other case

        fun create(): YoutubeAPI = create(HttpUrl.parse(YOUTUBE_BASE_URL)!!)
        private fun create(httpUrl: HttpUrl): YoutubeAPI {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.d("SearchVideoAPI", it)
            })

            logger.level = HttpLoggingInterceptor.Level.BASIC

            val okHttpClient = OkHttpClient.Builder().addInterceptor(logger).build()

            return Retrofit.Builder()
                    .baseUrl(httpUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(YoutubeAPI::class.java)
        }
    }
}

class ListingResponse(val data: ListingData)

class ListingData(
        val items: List<RedditChildrenResponse>,
        val nextPageToken: String?,
        val prevPageToken: String?
)

data class RedditChildrenResponse(val data: VideoModel)