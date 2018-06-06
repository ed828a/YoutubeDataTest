package com.dew.edward.youtubedatatest

import android.arch.lifecycle.ViewModelProvider
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.View
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YouTubeUriExtractor
import at.huber.youtubeExtractor.YtFile
import com.dew.edward.youtubedatatest.model.ChannelModel

import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import kotlinx.android.synthetic.main.activity_video_play.*
import android.os.AsyncTask.execute
import android.system.Os.mkdir
import java.nio.file.Files.exists
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Environment
import android.os.PersistableBundle
import android.support.v7.widget.GridLayoutManager
import com.dew.edward.youtubedatatest.adapters.RelatedVideoAdapter
import com.dew.edward.youtubedatatest.fragments.extractDate
import com.dew.edward.youtubedatatest.model.RelatedVideoModel
import com.dew.edward.youtubedatatest.modules.*
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection


class VideoPlayActivity : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener {

    lateinit var channelModel: ChannelModel
    lateinit var relatedVideoGetUrl: String
    val relatedVideoList = ArrayList<RelatedVideoModel>()
    var isRelatedVideo: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        channelModel = intent.getParcelableExtra(CHANNEL_MODEL)

        relatedVideoGetUrl = SEARCH_RELATED_PART1 + channelModel.videoId + SEARCH_RELATED_PART2
        Log.e("Rotate", "Related Video Url: $relatedVideoGetUrl")


        youtubePlayer.initialize(API_KEY, this)
        textVideoPlayTitle?.text = channelModel.title

        recyclerRelatedListView?.layoutManager = GridLayoutManager(this, 2)
        recyclerRelatedListView?.adapter = RelatedVideoAdapter(this, relatedVideoList) {

            App.mYoutubePlayer?.release()
            youtubePlayer.initialize(API_KEY, this)
            textVideoPlayTitle?.text = it.title
            channelModel = ChannelModel(it.title, it.channelTitle, it.publishedAt, it.thumbNail, it.videoId)
            relatedVideoGetUrl = SEARCH_RELATED_PART1 + it.videoId + SEARCH_RELATED_PART2
            isRelatedVideo = true

            RequestYoutubeAPI().execute()
        }

        RequestYoutubeAPI().execute()
    }

    override fun onInitializationSuccess(provider: YouTubePlayer.Provider?, player: YouTubePlayer?, wasRestored: Boolean) {
        player?.setPlayerStateChangeListener(playerStateChangeListener)
        player?.setPlaybackEventListener(playbackEventListener)
        if (!wasRestored) {
            player?.cueVideo(channelModel.videoId)
        }

        if (isRelatedVideo){
            player?.cueVideo(channelModel.videoId)
            isRelatedVideo = false
        }

        if (player != null) {
            Log.e("Rotate", "App.mYoutubePlayer = player:  ${player?.toString()}")
            App.mYoutubePlayer = player
        }
    }

    private val playerStateChangeListener: YouTubePlayer.PlayerStateChangeListener =
            object : YouTubePlayer.PlayerStateChangeListener {
                override fun onAdStarted() {
                }

                override fun onLoading() {
                }

                override fun onVideoStarted() {
                }

                override fun onLoaded(videoId: String?) {
                    Log.e("Rotate", "onLoaded: $videoId")
                    App.mYoutubePlayer?.play()
                }

                override fun onVideoEnded() {
                }

                override fun onError(p0: YouTubePlayer.ErrorReason?) {
                }

            }

    private val playbackEventListener: YouTubePlayer.PlaybackEventListener =
            object : YouTubePlayer.PlaybackEventListener {
                override fun onSeekTo(p0: Int) {
                }

                override fun onBuffering(p0: Boolean) {
                }

                override fun onPlaying() {
                }

                override fun onStopped() {
                }

                override fun onPaused() {
                }

            }

    override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {

    }

    fun downloadVideo(view: View) {
        // video id should get out of Channel Model
        val link = "https://www.youtube.com/watch?=${channelModel.videoId}"
        Log.d("Download", "downloadVideo: $link")
        // not completed yet
    }

    inner class RequestYoutubeAPI : AsyncTask<Void, String, String>() {

        override fun doInBackground(vararg params: Void?): String {
            val httpClient: HttpClient = DefaultHttpClient()
            val httpGet: HttpGet = HttpGet(relatedVideoGetUrl)
            Log.e("URL", relatedVideoGetUrl)


            var json: String = ""
            try {
                val response: HttpResponse = httpClient.execute(httpGet)
                val httpEntity = response.entity
                json = EntityUtils.toString(httpEntity)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return json
        }

        override fun onPostExecute(response: String?) {
            super.onPostExecute(response)
            if (response != null) {
                try {
                    val jsonObject: JSONObject = JSONObject(response)
                    Log.e("RESPONSE", jsonObject.toString())
                    parseVideoListFromResponse(jsonObject)
                    recyclerRelatedListView.adapter.notifyDataSetChanged()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun parseVideoListFromResponse(jsonObject: JSONObject) {

            if (jsonObject.has("items")) {
                relatedVideoList.clear()
                try {
                    val jsonArray: JSONArray = jsonObject.getJSONArray("items")
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray[i] as JSONObject
                        if (json.has("id")) {
                            val jsonID = json.getJSONObject("id")
                            var video_id = ""
                            if (jsonID.has("videoId")) {
                                video_id = jsonID.getString("videoId")
                            }
                            if (jsonID.has("kind")) {
                                if (jsonID.getString("kind").equals("youtube#video")) {
                                    val jsonSnippet = json.getJSONObject("snippet")
                                    val title = jsonSnippet.getString("title")
                                    val channelTitle = jsonSnippet.getString("channelTitle")
                                    val publishedAt = jsonSnippet.getString("publishedAt").extractDate()
                                    Log.d("Strings", publishedAt)
                                    val thumbnail = jsonSnippet.getJSONObject("thumbnails")
                                            .getJSONObject("high").getString("url")

                                    val relatedVideoModel = RelatedVideoModel(title,
                                            channelTitle, publishedAt, thumbnail, video_id)

                                    relatedVideoList.add(relatedVideoModel)
                                }
                            }
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

    }

    override fun onDestroy() {

        if (App.mYoutubePlayer != null){
            App.mYoutubePlayer?.release()
        }

        super.onDestroy()
    }
}

