package com.dew.edward.youtubedatatest.modules

import android.app.Application
import com.google.android.youtube.player.YouTubePlayer

/*
 * Created by Edward on 6/6/2018.
 */

class App:Application() {
    companion object {
        var mYoutubePlayer: YouTubePlayer? = null
    }

}