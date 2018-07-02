package com.dew.edward.youtubedatatest.controllers

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import com.commit451.youtubeextractor.YouTubeExtraction
import com.commit451.youtubeextractor.YouTubeExtractor
import com.dew.edward.youtubedatatest.R
import com.dew.edward.youtubedatatest.adapters.RelatedVideoAdapter
import com.dew.edward.youtubedatatest.api.YoutubeAPI
import com.dew.edward.youtubedatatest.model.ChannelModel
import com.dew.edward.youtubedatatest.modules.CHANNEL_MODEL
import com.dew.edward.youtubedatatest.modules.SEARCH_RELATED_PART1
import com.dew.edward.youtubedatatest.modules.SEARCH_RELATED_PART2
import com.dew.edward.youtubedatatest.repository.YoutubeAPIRequest
import com.dew.edward.youtubedatatest.util.MY_PERMISSIONS_REQUEST
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
import com.google.android.exoplayer2.ui.PlaybackControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_new_exo_player.*
import kotlinx.android.synthetic.main.custom_playback_control.view.*

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.*
import java.util.concurrent.Executors

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
    private var videoUrl: String = ""
    private lateinit var mExoPlayerView: PlayerView
    private lateinit var fullscreenButton: FrameLayout
    private lateinit var fullscreenIcon: ImageView
    private lateinit var fullscreenDialog: Dialog
    private var isExoPlayerFullscreen: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_exo_player)

        playerView = findViewById(R.id.video_view)

        if (ContextCompat.checkSelfPermission(this@NewExoPlayerActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@NewExoPlayerActivity, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST)
        }

        Log.d("writeResponseBodyToDisk", "onCreate: isExternalStorageWritable: ${Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED}")

        if (savedInstanceState != null) {
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

    private fun initFullscreenButton(){
        val controlView = mExoPlayerView.findViewById<PlaybackControlView>(R.id.exo_controller)
        val duration = controlView.exo_duration
        val position = controlView.exo_position
//        val fullscreen = controlView.fullscreen_button
//        fullscreenButton = controlView.exo_fullscreen_button
//        fullscreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon)
//
//        fullscreenButton.setOnClickListener {
//            if (!isExoPlayerFullscreen){
//                openFullscreenDialog()
//            } else {
//                closeFullscreenDialog()
//            }
//        }
    }

    private fun initFullscreenDialog(){
        fullscreenDialog = object : Dialog(this@NewExoPlayerActivity,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen){
            override fun onBackPressed() {
                if (isExoPlayerFullscreen)
                    closeFullscreenDialog()
                super.onBackPressed()
            }
        }
    }

    private fun openFullscreenDialog(){
        (mExoPlayerView.parent as ViewGroup).removeView(mExoPlayerView)
        fullscreenDialog.addContentView(mExoPlayerView,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        fullscreenIcon.setImageDrawable(ContextCompat.getDrawable(this@NewExoPlayerActivity, R.drawable.exo_controls_fullscreen_exit))
        isExoPlayerFullscreen = true
        fullscreenDialog.show()
    }

    private fun closeFullscreenDialog(){
        (mExoPlayerView.parent as ViewGroup).removeView(mExoPlayerView)
//        exoPlayerFrameLayout.addView(mExoPlayerView)
        isExoPlayerFullscreen = false
        fullscreenDialog.dismiss()
        fullscreenIcon.setImageDrawable(ContextCompat.getDrawable(this@NewExoPlayerActivity, R.drawable.exo_controls_fullscreen_enter))
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
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer(this, videoUrl)
        }
        mExoPlayerView = video_view as PlayerView
//        initFullscreenDialog()
//        initFullscreenButton()

        val userAgent = Util.getUserAgent(this@NewExoPlayerActivity, getString(R.string.app_name))
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

    fun onClickDownload(view: View) {
//        val videoUrl = "https://i.ytimg.com/vi/Xt7E8MxWGlM/mqdefault.jpg"
        val myVideoUrl = "https://r3---sn-ppoxu-5qaz.googlevideo.com/videoplayback?pl=19&fvip=5&ei=H0c3W8mVHJKV4ALz5YugBw&itag=22&ratebypass=yes&fexp=23709359&initcwndbps=741250&requiressl=yes&mime=video%2Fmp4&key=yt6&source=youtube&id=o-AHshDWX9cA5qrKl-44o19_c7u2RoVIzWmYPyjBHF2NfY&ipbits=0&mm=31%2C29&mn=sn-ppoxu-5qaz%2Csn-ntqe6n76&dur=919.394&expire=1530370943&c=WEB&lmt=1526037023302964&ip=14.2.87.57&ms=au%2Crdu&mt=1530349195&mv=m&sparams=dur%2Cei%2Cid%2Cinitcwndbps%2Cip%2Cipbits%2Citag%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpl%2Cratebypass%2Crequiressl%2Csource%2Cexpire&signature=695C70C64091FB7BEE21BA26223B855B1FB3C249.93A5E5578A2F134C01ABECD9A394B544F266038B"
        val downloadAPIRetrofit = Retrofit.Builder()
                .baseUrl("https://r3---sn-ppoxu-5qaz.googlevideo.com/")
                .build()
        val downloadApi = downloadAPIRetrofit.create(YoutubeAPI::class.java)
        val call = downloadApi.downloadVideoByUrlStream(myVideoUrl)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {

                Toast.makeText(this@NewExoPlayerActivity, "download failed", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {

                Executors.newSingleThreadExecutor().execute {
                    Log.d("writeResponseBodyToDisk", "file downloading started. ")
                    val isSuccess = writeResponseBodyToDisk(response!!.body()!!)
                    Log.d("writeResponseBodyToDisk", "was file download successful: $isSuccess ")
                }
            }
        })
    }

    private fun writeResponseBodyToDisk(body: ResponseBody): Boolean {

        val fileFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        Log.d("writeResponseBodyToDisk", "filename = ${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}")
        val file = File(fileFolder, "mqdefault.mp4")
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            val fileReader = ByteArray(4096)

            val fileSize = body.contentLength()
            var fileSizeDownloaded: Long = 0

            inputStream = body.byteStream()
            outputStream = FileOutputStream(file)

            while (true) {
                val read = inputStream.read(fileReader)

                if (read == -1) {
                    break
                }

                outputStream.write(fileReader, 0, read)

                fileSizeDownloaded += read.toLong()

                Log.d("writeResponseBodyToDisk", "file download: $fileSizeDownloaded of $fileSize")
            }

            outputStream.flush()

            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            inputStream?.close()
            outputStream?.close()

        }
    }
}
