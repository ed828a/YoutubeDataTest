package com.dew.edward.youtubedatatest.fragments


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dew.edward.youtubedatatest.R
import com.dew.edward.youtubedatatest.VideoPlayActivity
import com.dew.edward.youtubedatatest.adapters.ChannelPostAdapter
import com.dew.edward.youtubedatatest.model.ChannelModel
import com.dew.edward.youtubedatatest.modules.CHANNEL_GET_URL
import com.dew.edward.youtubedatatest.modules.CHANNEL_MODEL
import com.dew.edward.youtubedatatest.modules.SEARCH_GET_URL
import com.dew.edward.youtubedatatest.repository.YoutubeAPIRequest
import com.dew.edward.youtubedatatest.repository.extractDate
import kotlinx.android.synthetic.main.fragment_channel.*
import kotlinx.android.synthetic.main.fragment_channel.view.*
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


/**
 * A simple [Fragment] subclass.
 *
 */
class ChannelFragment : Fragment() {

    lateinit var mListView: RecyclerView
    var mListData: ArrayList<ChannelModel> = arrayListOf(ChannelModel(
            "Grenfell Inquiry: How the fire started and spread",
            "The Grenfell Tower inquiry is now examining how the fire started and spread. Using witness evidence and testimony from the emergency services and residents, ...",
            "2018-06-05",
            "https://i.ytimg.com/vi/zzhrIZCm2qk/hqdefault_live.jpg", "bVztmlmvPOw"))



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_channel, container, false)

        mListView = view.channelListView

        initList(mListData)
//        RequestYoutubeAPI().execute()
        YoutubeAPIRequest(mListData, SEARCH_GET_URL, mListView.adapter).execute()

        return view
    }

    private fun initList(listData : ArrayList<ChannelModel>) {

        mListView.adapter = ChannelPostAdapter(activity as Context, listData){
            channelModel ->

            val intent = Intent(activity, VideoPlayActivity::class.java)
            Log.d("initList", channelModel.toString())
            intent.putExtra(CHANNEL_MODEL, channelModel)
            startActivity(intent)
        }

    }

    // create as asynctask to get all the data from youtube

    @SuppressLint("StaticFieldLeak")
    inner class RequestYoutubeAPI: AsyncTask<Void, String, String>(){

        override fun doInBackground(vararg params: Void?): String {
            val httpClient: HttpClient = DefaultHttpClient()
            val httpGet: HttpGet = HttpGet(SEARCH_GET_URL)
            Log.e("URL", SEARCH_GET_URL)


            var json: String =""
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
            if (response != null){
                try {
                    val jsonObject: JSONObject = JSONObject(response)
                    Log.e("RESPONSE", jsonObject.toString())
                    parseVideoListFromResponse(jsonObject)
                    mListView.adapter.notifyDataSetChanged()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun parseVideoListFromResponse(jsonObject: JSONObject) {

            if (jsonObject.has("items")){
                mListData.clear()
                try {
                    val jsonArray:JSONArray = jsonObject.getJSONArray("items")
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray[i] as JSONObject
                        if (json.has("id")){
                            val jsonID = json.getJSONObject("id")
                            var video_id = ""
                            if (jsonID.has("videoId")){
                                video_id = jsonID.getString("videoId")
                            }
                            if (jsonID.has("kind")){
                                if (jsonID.getString("kind").equals("youtube#video")){
                                    val jsonSnippet = json.getJSONObject("snippet")
                                    val title = jsonSnippet.getString("title")
                                    val channelTitle = jsonSnippet.getString("channelTitle")
                                    val publishedAt = jsonSnippet.getString("publishedAt").extractDate()
                                    Log.d("Strings", publishedAt)
                                    val thumbnail = jsonSnippet.getJSONObject("thumbnails")
                                            .getJSONObject("high").getString("url")

                                    val youtubeModel = ChannelModel(title, channelTitle,
                                            publishedAt, thumbnail, video_id)
                                    mListData.add(youtubeModel)
                                }
                            }
                        }
                    }
                } catch (e: Throwable){
                    e.printStackTrace()
                }
            }
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

    }
}


