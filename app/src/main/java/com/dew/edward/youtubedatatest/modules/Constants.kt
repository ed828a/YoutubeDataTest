package com.dew.edward.youtubedatatest.modules

/*
 * Created by Edward on 6/5/2018.
 */
 
const val API_KEY = "AIzaSyA7cdJ8OPftCtkqIBpVuIX5CVtY7BW02JU"
const val CHANNEL_ID ="UCoMdktPbSTixAyNGwb-UYkQ"
const val CHANNEL_GET_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&order=date&channelId=$CHANNEL_ID&maxResults=20&key=$API_KEY"
const val CHANNEL_MODEL= "channel model"
const val SEARCH_GET_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=25&q=movie+trailer+2018&key=$API_KEY"
const val SEARCH_RELATED_PART1 = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=25&relatedToVideoId="
const val SEARCH_RELATED_PART2 ="&type=video&key=$API_KEY"
// GET https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=25&relatedToVideoId=Ks-_Mh1QhMc&type=video&key={YOUR_API_KEY}
