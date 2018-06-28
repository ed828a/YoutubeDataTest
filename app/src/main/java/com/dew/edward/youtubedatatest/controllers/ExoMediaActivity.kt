package com.dew.edward.youtubedatatest.controllers

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
import com.dew.edward.youtubedatatest.viewmodels.QueryUrlViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_exo_media.*
import okhttp3.OkHttpClient

class ExoMediaActivity : AppCompatActivity() {

    lateinit var channelModel: ChannelModel
    lateinit var relatedVideoGetUrl: String
    val relatedVideoList = ArrayList<ChannelModel>()
    var isRelatedVideo: Boolean = false
    lateinit var listView: RecyclerView
    lateinit var queryViewModel: QueryUrlViewModel

    val okHttpClientBuilder: OkHttpClient.Builder? = null
    val extractor = YouTubeExtractor.Builder().okHttpClientBuilder(okHttpClientBuilder).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exo_media)

        channelModel = intent.getParcelableExtra(CHANNEL_MODEL)

        relatedVideoGetUrl = SEARCH_RELATED_PART1 + channelModel.videoId + SEARCH_RELATED_PART2
        Log.d("Rotate", "Related Video Url: $relatedVideoGetUrl")

        Log.d("ExoMediaActivity", "channelModel.videoId: ${channelModel.videoId}")
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

        exoMediaPlayer.setOnPreparedListener{
            exoMediaPlayer.volume = 1.0f
            exoMediaPlayer.seekTo(0L)
            exoMediaPlayer.start()
        }

        exoMediaPlayer.setOnErrorListener { e ->
            e.printStackTrace()
            false
        }

//        queryViewModel = QueryUrlViewModel()
//        if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
//            textExoVideoPlayTitle?.text = channelModel.title
//
//            listView = recyclerExoRelatedListView
//            listView.layoutManager = GridLayoutManager(this, 2)
//            listView.adapter = RelatedVideoAdapter(this, relatedVideoList) {
//
//                //                VideoApp.mYoutubePlayer?.release()
//                extractor.extract(channelModel.videoId)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(
//                                { extraction ->
//                                    bindVideoToPlayer(extraction)
//                                },
//                                { error ->
//                                    errorHandler(error)
//                                }
//                        )
//
//                textExoVideoPlayTitle?.text = it.title
//                channelModel = ChannelModel(it.title, it.channelTitle, it.publishedAt, it.thumbNail, it.videoId)
//                relatedVideoGetUrl = SEARCH_RELATED_PART1 + it.videoId + SEARCH_RELATED_PART2
//                isRelatedVideo = true
//                intent.putExtra(CHANNEL_MODEL, it)
//
//                YoutubeAPIRequest(relatedVideoList, relatedVideoGetUrl, listView.adapter).execute()
//            }
//
//            YoutubeAPIRequest(relatedVideoList, relatedVideoGetUrl, listView.adapter).execute()
//            buttonExoSearch.setOnSearchClickListener {
//                buttonExoDownload.visibility = View.GONE
//                textExoVideoPlayTitle.visibility = View.GONE
//
//                buttonExoSearch.onActionViewExpanded()
//
//            }
//            buttonExoSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//                override fun onQueryTextSubmit(query: String?): Boolean {
//                    var queryString: String = ""
//                    val strings: List<String>? = query?.split(" ")
//                    if (strings != null && strings.isNotEmpty()) {
//                        for (position in 0 until strings.size) {
//                            queryString = if (position == 0) {
//                                strings[position]
//                            } else {
//                                "$queryString+${strings[position]}"
//                            }
//                        }
//                    }
//                    queryViewModel.query = queryString
//
//                    buttonExoSearch.onActionViewCollapsed()
////                hideKeyboard()
//                    buttonExoDownload.visibility = View.VISIBLE
//                    textExoVideoPlayTitle.visibility = View.VISIBLE
//
//                    Log.d("QUERY", "queryURL: ${queryViewModel.getYoutubeQueryUrl()}")
//                    YoutubeAPIRequest(relatedVideoList, queryViewModel.getYoutubeQueryUrl(),
//                            listView.adapter).execute()
//                    return false
//                }
//
//                override fun onQueryTextChange(newText: String?): Boolean {
//                    return false
//                }
//            })
//        }
    }

    override fun onResume() {
        super.onResume()
        exoMediaPlayer.seekTo(0L)
        exoMediaPlayer.start()
    }

    fun downloadVideo(view: View) {
        // video id should get out of Channel Model
        val link = "https://www.youtube.com/watch?=${channelModel.videoId}"
        Log.d("Download", "downloadVideo: $link")
        // not completed yet
    }

    private fun errorHandler(t: Throwable) {
        t.printStackTrace()
        Toast.makeText(this@ExoMediaActivity, "It failed to extract URL from YouTube.", Toast.LENGTH_SHORT).show()
    }

    private fun bindVideoToPlayer(result: YouTubeExtraction) {
        val videoUrl = result.videoStreams.first().url
        Log.d("ExoMediaActivity", "videoUrl: $videoUrl")
        exoMediaPlayer.setVideoURI(Uri.parse(videoUrl))
    }

}
