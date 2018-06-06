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
import com.dew.edward.youtubedatatest.modules.API_KEY
import com.dew.edward.youtubedatatest.modules.CHANNEL_MODEL

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
import com.dew.edward.youtubedatatest.modules.App
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection


class VideoPlayActivity : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener {

    lateinit var channelModel: ChannelModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        channelModel = intent.getParcelableExtra(CHANNEL_MODEL)
        Log.e("Rotate", "onCreate: initialize")
        youtubePlayer.initialize(API_KEY, this)


    }

    override fun onInitializationSuccess(provider: YouTubePlayer.Provider?, player: YouTubePlayer?, wasRestored: Boolean) {
        player?.setPlayerStateChangeListener(playerStateChangeListener)
        player?.setPlaybackEventListener(playbackEventListener)
        if (!wasRestored) {
            player?.cueVideo(channelModel.videoId)
            if (player != null) {
                App.mYoutubePlayer = player
            }
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

        val ytEx = object : YouTubeUriExtractor(this) {
            override fun onUrisAvailable(videoID: String, videoTitle: String, ytFiles: SparseArray<YtFile>?) {
                if (ytFiles != null) {
                    val itag = 22
                    //This is the download URL
                    val downloadURL = ytFiles.get(itag).url
                    Log.e("download URL :", downloadURL)

                    //now download it like a file
                    RequestDownloadVideoStream().execute(downloadURL, videoTitle)


                }

            }
        }


//        ytEx.execute(link)

    }

    private inner class RequestDownloadVideoStream : AsyncTask<String, String, String>() {
        private val pDialog = ProgressDialog(this@VideoPlayActivity)
        override fun onPreExecute() {
            super.onPreExecute()

            pDialog.setMessage("Downloading file. Please wait...")
            pDialog.setIndeterminate(false)
            pDialog.setMax(100)
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            pDialog.setCancelable(false)
            pDialog.show()
        }

        override fun doInBackground(vararg params: String): String? {
            var `is`: InputStream? = null
            var u: URL? = null
            var len1 = 0
            var temp_progress = 0
            var progress = 0
            try {
                u = URL(params[0])
                `is` = u!!.openStream()
                val huc = u!!.openConnection() as URLConnection
                huc!!.connect()
                val size = huc!!.getContentLength()

                if (huc != null) {
                    val file_name = params[1] + ".mp4"
                    val storagePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/YoutubeVideos"
                    val f = File(storagePath)
                    if (!f.exists()) {
                        f.mkdir()
                    }

                    val fos = FileOutputStream("$f/$file_name")
                    val buffer = ByteArray(1024)
                    var total = 0
                    if (`is` != null) {
                        val len1 = `is`!!.read(buffer)
                        while (len1 != -1) {
                            total += len1
                            // publishing the progress....
                            // After this onProgressUpdate will be called
                            progress = (total * 100 / size).toInt()
                            if (progress >= 0) {
                                temp_progress = progress
                                publishProgress("" + progress)
                            } else
                                publishProgress("" + temp_progress + 1)

                            fos.write(buffer, 0, len1)
                        }
                    }

                    if (fos != null) {
                        publishProgress("" + 100)
                        fos.close()
                    }
                }
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (`is` != null) {
                    try {
                        `is`!!.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
            return null
        }

        override fun onProgressUpdate(vararg values: String) {
            super.onProgressUpdate(*values)
            pDialog.setProgress(Integer.parseInt(values[0]))
        }

        override fun onPostExecute(s: String) {
            super.onPostExecute(s)
            if (pDialog.isShowing())
                pDialog.dismiss()
        }
    }

}

