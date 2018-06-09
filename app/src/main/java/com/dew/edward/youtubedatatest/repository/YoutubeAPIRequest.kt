package com.dew.edward.youtubedatatest.repository

import android.os.AsyncTask
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.dew.edward.youtubedatatest.model.ChannelModel
import com.dew.edward.youtubedatatest.modules.GET_REQUEST_URL
import com.dew.edward.youtubedatatest.modules.GET_RESPONSE
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/*
 * Created by Edward on 6/8/2018.
 */

class YoutubeAPIRequest(private val dataList: ArrayList<ChannelModel>,
                        private val getRequestUrl: String,
                        val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) :
        AsyncTask<Void, String, String>() {

    override fun doInBackground(vararg params: Void?): String {
        val httpClient = DefaultHttpClient()
        val httpGet = HttpGet(getRequestUrl)
        Log.e(GET_REQUEST_URL, getRequestUrl)

        var json = ""
        try {
            val response: HttpResponse = httpClient.execute(httpGet)
            val httpEntity: HttpEntity = response.entity
            json = EntityUtils.toString(httpEntity)
        } catch (e: IOException){
            e.printStackTrace()
        }

        return json
    }

    override fun onPostExecute(response: String?) {
        super.onPostExecute(response)

        Log.d(GET_RESPONSE, response)
        if (response != null){
            try {
                val jsonObject = JSONObject(response)
                parseVideoListFromResponse(jsonObject, dataList)
                Log.d("REQUEST", dataList[0].toString())
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun parseVideoListFromResponse(jsonObject: JSONObject,
                                           dataList: ArrayList<ChannelModel>){
        if (jsonObject.has("items")){
            dataList.clear()
            try {
                val jsonArray:JSONArray = jsonObject.getJSONArray("items")
                for (i in 0 until jsonArray.length()){
                    val json = jsonArray[i] as JSONObject
                    if (json.has("id")){
                        val jsonID: JSONObject = json.getJSONObject("id")
                        var video_id = ""
                        if (jsonID.has("videoId")){
                            video_id = jsonID.getString("videoId")
                        }
                        if (jsonID.has("kind") &&
                                jsonID.getString("kind").equals("youtube#video")){
                            val jsonSnippet: JSONObject = json.getJSONObject("snippet")
                            val title: String = jsonSnippet.getString("title")
                            val channelTitle: String = jsonSnippet.getString("channelTitle")
                            val publishedAt: String = jsonSnippet.getString("publishedAt").extractDate()
                            val thumbnail: String = jsonSnippet.getJSONObject("thumbnails")
                                    .getJSONObject("high").getString("url")

                            val youtubeModel = ChannelModel(title, channelTitle, publishedAt,
                                    thumbnail, video_id)

                            dataList.add(youtubeModel)

                        }
                    }
                }
            } catch (e: Throwable){
                e.printStackTrace()
            }
        }
    }
}

fun String.extractDate(): String {
    val stringArray = this.split('T')
    return stringArray[0]

}