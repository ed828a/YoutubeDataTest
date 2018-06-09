package com.dew.edward.youtubedatatest.viewmodels

import android.arch.lifecycle.ViewModel
import com.dew.edward.youtubedatatest.modules.API_KEY

/*
 * Created by Edward on 6/8/2018.
 */

class QueryUrlViewModel: ViewModel() {
    var query: String = "movie+trailers+2018"
    // "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&q=movie+trailer+2018&key=$API_KEY"
    val queryHeader = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&q="
    val queryTail = "&key=$API_KEY"


    fun getYoutubeQueryUrl(): String  = "$queryHeader$query$queryTail"
}