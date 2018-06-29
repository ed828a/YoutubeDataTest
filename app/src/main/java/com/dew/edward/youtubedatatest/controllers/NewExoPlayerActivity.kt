package com.dew.edward.youtubedatatest.controllers

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.View
import android.widget.Toast
import com.commit451.youtubeextractor.YouTubeExtraction
import com.commit451.youtubeextractor.YouTubeExtractor
import com.dew.edward.youtubedatatest.R
import com.dew.edward.youtubedatatest.adapters.RelatedVideoAdapter
import com.dew.edward.youtubedatatest.model.ChannelModel
import com.dew.edward.youtubedatatest.modules.CHANNEL_MODEL
import com.dew.edward.youtubedatatest.modules.SEARCH_RELATED_PART1
import com.dew.edward.youtubedatatest.modules.SEARCH_RELATED_PART2
import com.dew.edward.youtubedatatest.repository.YoutubeAPIRequest
import com.dew.edward.youtubedatatest.util.PLAYBACKPOSITION
import com.dew.edward.youtubedatatest.viewmodels.QueryUrlViewModel
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_new_exo_player.*
import okhttp3.OkHttpClient

class NewExoPlayerActivity : AppCompatActivity() {

    lateinit var channelModel: ChannelModel
    lateinit var relatedVideoGetUrl: String
    val relatedVideoList = ArrayList<ChannelModel>()
    var isRelatedVideo: Boolean = false
    lateinit var listView: RecyclerView
    lateinit var queryViewModel: QueryUrlViewModel

    private val okHttpClientBuilder: OkHttpClient.Builder? = null
    val extractor = YouTubeExtractor.Builder().okHttpClientBuilder(okHttpClientBuilder).build()

    // bandwidth meter to measure and estimate bandwidth
    private val BANDWIDTH_METER = DefaultBandwidthMeter()
    private var player: SimpleExoPlayer? = null
    private var playerView: PlayerView? = null
    private var playbackPosition: Long = 0
    private var currentWindow: Int = 0
    private var playWhenReady = true
    private var videoUrl: String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_exo_player)

        playerView = findViewById(R.id.video_view)

        if (savedInstanceState != null){
            playbackPosition = savedInstanceState.getLong(PLAYBACKPOSITION)
            Log.d("onCreate", "playbackPosition = $playbackPosition")
        }

        channelModel = intent.getParcelableExtra(CHANNEL_MODEL)

        relatedVideoGetUrl = SEARCH_RELATED_PART1 + channelModel.videoId + SEARCH_RELATED_PART2
        Log.d("Rotate", "Related Video Url: $relatedVideoGetUrl")

        extractor.extract(channelModel.videoId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { extraction ->
                            bindVideoToPlayer(extraction)
                        },
                        { error ->
                            errorHandler(error)
                        }
                )

        queryViewModel = QueryUrlViewModel()
        if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
            textExoPlayerTitle?.text = channelModel.title

            listView = recyclerExoPlayerRelatedListView
            listView.layoutManager = GridLayoutManager(this, 2) as RecyclerView.LayoutManager?
            listView.adapter = RelatedVideoAdapter(this, relatedVideoList) {

                //                VideoApp.mYoutubePlayer?.release()
                extractor.extract(it.videoId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { extraction ->
                                    bindVideoToPlayer(extraction)
                                },
                                { error ->
                                    errorHandler(error)
                                }
                        )

                textExoPlayerTitle?.text = it.title
                channelModel = ChannelModel(it.title, it.channelTitle, it.publishedAt, it.thumbNail, it.videoId)
                relatedVideoGetUrl = SEARCH_RELATED_PART1 + it.videoId + SEARCH_RELATED_PART2
                isRelatedVideo = true
                intent.putExtra(CHANNEL_MODEL, it)
                playbackPosition = 0

                YoutubeAPIRequest(relatedVideoList, relatedVideoGetUrl, listView.adapter).execute()
            }

            YoutubeAPIRequest(relatedVideoList, relatedVideoGetUrl, listView.adapter).execute()
            buttonExoPlayerSearch.setOnSearchClickListener {
                buttonExoPlayerDownload.visibility = View.GONE
                textExoPlayerTitle.visibility = View.GONE

                buttonExoPlayerSearch.onActionViewExpanded()

            }
            buttonExoPlayerSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    var queryString: String = ""
                    val strings: List<String>? = query?.split(" ")
                    if (strings != null && strings.isNotEmpty()) {
                        for (position in 0 until strings.size) {
                            queryString = if (position == 0) {
                                strings[position]
                            } else {
                                "$queryString+${strings[position]}"
                            }
                        }
                    }
                    queryViewModel.query = queryString

                    buttonExoPlayerSearch.onActionViewCollapsed()
//                hideKeyboard()
                    buttonExoPlayerDownload.visibility = View.VISIBLE
                    textExoPlayerTitle.visibility = View.VISIBLE

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

    private fun bindVideoToPlayer(result: YouTubeExtraction) {
        val videoUrl = result.videoStreams.first().url
        Log.d("ExoMediaActivity", "videoUrl: $videoUrl")
        if (player != null) {
            releasePlayer()
        }
        initializePlayer(this, videoUrl)
    }

    private fun errorHandler(t: Throwable) {
        t.printStackTrace()
        Toast.makeText(this, "It failed to extract URL from YouTube.", Toast.LENGTH_SHORT).show()
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer(this, videoUrl)
        }
    }

    public override fun onResume() {
        super.onResume()
//        hideSystemUi()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer(this, videoUrl)
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initializePlayer(context: Context, stringUrl: String) {
        if (player == null) {
            // a factory to create an AdaptiveVideoTrackSelection
            val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory(BANDWIDTH_METER)
            // let the factory create a player instance with default components
            player = ExoPlayerFactory.newSimpleInstance(
                    DefaultRenderersFactory(this),
                    DefaultTrackSelector(),
                    DefaultLoadControl())
            playerView!!.player = player
            player!!.playWhenReady = playWhenReady
            player!!.seekTo(currentWindow, playbackPosition)
            Log.d("initializePlayer", "playbackPosition = $playbackPosition")
        }
        val uri = Uri.parse(stringUrl)
        val mediaSource = buildMediaSource(uri)
        player!!.prepare(mediaSource, false, false)
    }

    private fun releasePlayer() {
        if (player != null) {
//            playbackPosition = player!!.currentPosition
            Log.d("releasePlayer", "playbackPosition = $playbackPosition")
            currentWindow = player!!.currentWindowIndex
            playWhenReady = player!!.playWhenReady
            player!!.release()
            player = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        playbackPosition = player?.currentPosition ?: 0
        Log.d("onSaveInstanceState", "playbackPosition = $playbackPosition")
        outState?.putLong(PLAYBACKPOSITION, playbackPosition)
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        return ExtractorMediaSource.Factory(
                DefaultHttpDataSourceFactory("exoplayer-codelab")).createMediaSource(uri)
    }

    private fun buildMediaSource2(uri: Uri): MediaSource {
        val dashChunkSourceFactory = DefaultDashChunkSource.Factory(
                DefaultHttpDataSourceFactory("ua", BANDWIDTH_METER))
        val manifestDataSourceFactory = DefaultHttpDataSourceFactory("ua")
        return DashMediaSource.Factory(dashChunkSourceFactory, manifestDataSourceFactory).createMediaSource(uri)
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        playerView!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

}
