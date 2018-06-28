package com.dew.edward.youtubedatatest.controllers

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.View
import android.widget.ImageView
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
import com.dew.edward.youtubedatatest.viewmodels.QueryUrlViewModel
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.TransferListener
import com.google.android.exoplayer2.util.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_exo_player.*
import okhttp3.OkHttpClient

class ExoPlayerActivity : AppCompatActivity() {

    lateinit var channelModel: ChannelModel
    lateinit var relatedVideoGetUrl: String
    val relatedVideoList = ArrayList<ChannelModel>()
    var isRelatedVideo: Boolean = false
    lateinit var listView: RecyclerView
    lateinit var queryViewModel: QueryUrlViewModel

    private val okHttpClientBuilder: OkHttpClient.Builder? = null
    val extractor = YouTubeExtractor.Builder().okHttpClientBuilder(okHttpClientBuilder).build()


//    private var simpleExoPlayerView: SimpleExoPlayerView? = null
    private var player: SimpleExoPlayer? = null

    private lateinit var window: Timeline.Window
    private var mediaDataSourceFactory: DataSource.Factory? = null
//    private var trackSelector: DefaultTrackSelector? = null
    private var shouldAutoPlay: Boolean = false
//    private lateinit var bandwidthMeter: BandwidthMeter

    private var ivHideControllerButton: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exo_player)

        channelModel = intent.getParcelableExtra(CHANNEL_MODEL)

        relatedVideoGetUrl = SEARCH_RELATED_PART1 + channelModel.videoId + SEARCH_RELATED_PART2
        Log.d("Rotate", "Related Video Url: $relatedVideoGetUrl")

        shouldAutoPlay = true
        window = Timeline.Window()
        ivHideControllerButton = findViewById<View>(R.id.exo_controller) as ImageView?

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

                textExoPlayerTitle?.text = it.title
                channelModel = ChannelModel(it.title, it.channelTitle, it.publishedAt, it.thumbNail, it.videoId)
                relatedVideoGetUrl = SEARCH_RELATED_PART1 + it.videoId + SEARCH_RELATED_PART2
                isRelatedVideo = true
                intent.putExtra(CHANNEL_MODEL, it)

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
        initializePlayer(this@ExoPlayerActivity, videoUrl)
    }

    private fun errorHandler(t: Throwable) {
        t.printStackTrace()
        Toast.makeText(this@ExoPlayerActivity, "It failed to extract URL from YouTube.", Toast.LENGTH_SHORT).show()
    }

    private fun initializePlayer(context: Context, videoUrl: String) {

        exoPlayerView.requestFocus()
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        mediaDataSourceFactory = DefaultDataSourceFactory(context,
                Util.getUserAgent(this, "ExoPlayerSample"),
                bandwidthMeter as TransferListener<in DataSource>)

        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector)

        exoPlayerView.player = player
        exoPlayerView.player.playWhenReady = shouldAutoPlay
        /*        MediaSource mediaSource = new HlsMediaSource(Uri.parse("https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"),
                mediaDataSourceFactory, mainHandler, null);*/

        val extractorsFactory = DefaultExtractorsFactory()

        val mediaSource = ExtractorMediaSource(Uri.parse(videoUrl), mediaDataSourceFactory, extractorsFactory, null, null)

        (exoPlayerView.player as SimpleExoPlayer?)?.prepare(mediaSource)

        ivHideControllerButton?.setOnClickListener { exoPlayerView.hideController() }

    }

    private fun releasePlayer() {
        if (exoPlayerView.player != null) {
            shouldAutoPlay = exoPlayerView.player.playWhenReady
            exoPlayerView.player.release()
            player = null
//            trackSelector = null
        }
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
//            initializePlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) {
//            initializePlayer()
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
}
