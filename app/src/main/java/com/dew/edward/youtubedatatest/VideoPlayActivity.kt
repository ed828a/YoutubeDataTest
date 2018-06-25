package com.dew.edward.youtubedatatest

import android.arch.lifecycle.LifecycleService
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.View
import com.dew.edward.youtubedatatest.adapters.RelatedVideoAdapter
import com.dew.edward.youtubedatatest.model.ChannelModel
import com.dew.edward.youtubedatatest.modules.*
import com.dew.edward.youtubedatatest.repository.YoutubeAPIRequest
import com.dew.edward.youtubedatatest.util.VideoApp
import com.dew.edward.youtubedatatest.viewmodels.QueryUrlViewModel
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import kotlinx.android.synthetic.main.activity_video_play.*


class VideoPlayActivity : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener{

    lateinit var channelModel: ChannelModel
    lateinit var relatedVideoGetUrl: String
    val relatedVideoList = ArrayList<ChannelModel>()
    var isRelatedVideo: Boolean = false
    lateinit var listView: RecyclerView

    lateinit var queryViewModel: QueryUrlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        channelModel = intent.getParcelableExtra(CHANNEL_MODEL)

        relatedVideoGetUrl = SEARCH_RELATED_PART1 + channelModel.videoId + SEARCH_RELATED_PART2
        Log.e("Rotate", "Related Video Url: $relatedVideoGetUrl")

        youtubePlayer.initialize(API_KEY, this)

        queryViewModel = QueryUrlViewModel()

        if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
            textVideoPlayTitle?.text = channelModel.title

            listView = recyclerRelatedListView
            listView.layoutManager = GridLayoutManager(this, 2)
            listView.adapter = RelatedVideoAdapter(this, relatedVideoList) {

                VideoApp.mYoutubePlayer?.release()
                youtubePlayer.initialize(API_KEY, this)
                textVideoPlayTitle?.text = it.title
                channelModel = ChannelModel(it.title, it.channelTitle, it.publishedAt, it.thumbNail, it.videoId)
                relatedVideoGetUrl = SEARCH_RELATED_PART1 + it.videoId + SEARCH_RELATED_PART2
                isRelatedVideo = true
                intent.putExtra(CHANNEL_MODEL, it)  

                YoutubeAPIRequest(relatedVideoList, relatedVideoGetUrl, listView.adapter).execute()
            }

            YoutubeAPIRequest(relatedVideoList, relatedVideoGetUrl, listView.adapter).execute()
            buttonSearch.setOnSearchClickListener {
                buttonDownload.visibility = View.GONE
                textVideoPlayTitle.visibility = View.GONE

                buttonSearch.onActionViewExpanded()

            }
            buttonSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    var queryString: String =""
                    val strings: List<String>? = query?.split(" ")
                    if (strings != null && strings.isNotEmpty()){
                        for (position in 0 until strings.size){
                            queryString = if (position == 0){
                                strings[position]
                            } else {
                                "$queryString+${strings[position]}"
                            }
                        }
                    }
                    queryViewModel.query = queryString

                    buttonSearch.onActionViewCollapsed()
//                hideKeyboard()
                    buttonDownload.visibility = View.VISIBLE
                    textVideoPlayTitle.visibility = View.VISIBLE

                    Log.d("QUERY", "queryURL: ${queryViewModel.getYoutubeQueryUrl()}")
                    YoutubeAPIRequest(relatedVideoList, queryViewModel.getYoutubeQueryUrl(),
                            listView.adapter).execute()
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })
        }
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
            Log.e("Rotate", "App.mYoutubePlayer = player:  ${player.toString()}")
            VideoApp.mYoutubePlayer = player
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
                    VideoApp.mYoutubePlayer?.play()
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

    override fun onDestroy() {

        if (VideoApp.mYoutubePlayer != null){
            VideoApp.mYoutubePlayer?.release()
        }

        super.onDestroy()
    }
}

