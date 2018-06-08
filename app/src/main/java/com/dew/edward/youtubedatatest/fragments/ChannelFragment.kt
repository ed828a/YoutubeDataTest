package com.dew.edward.youtubedatatest.fragments


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dew.edward.youtubedatatest.R
import com.dew.edward.youtubedatatest.VideoPlayActivity
import com.dew.edward.youtubedatatest.adapters.ChannelPostAdapter
import com.dew.edward.youtubedatatest.model.ChannelModel
import com.dew.edward.youtubedatatest.modules.CHANNEL_MODEL
import com.dew.edward.youtubedatatest.modules.SEARCH_GET_URL
import com.dew.edward.youtubedatatest.repository.YoutubeAPIRequest
import kotlinx.android.synthetic.main.fragment_channel.view.*


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

}


