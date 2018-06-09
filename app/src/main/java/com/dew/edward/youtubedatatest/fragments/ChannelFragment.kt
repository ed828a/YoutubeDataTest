package com.dew.edward.youtubedatatest.fragments


import android.arch.lifecycle.ViewModelProviders
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
import com.dew.edward.youtubedatatest.viewmodels.QueryUrlViewModel
import kotlinx.android.synthetic.main.fragment_channel.view.*


/**
 * A simple [Fragment] subclass.
 *
 */
class ChannelFragment : Fragment() {

    lateinit var mListView: RecyclerView
    var mListData: ArrayList<ChannelModel> = arrayListOf()

    private val queryViewModel by lazy {
        ViewModelProviders.of(activity!!).get(QueryUrlViewModel::class.java)
    }
    lateinit var adapter: ChannelPostAdapter



//    lateinit var queryViewModel: QueryUrlViewModel



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_channel, container, false)
        Log.d("REQUEST", "onCreateView was hit: ${queryViewModel.getYoutubeQueryUrl()}")
        mListView = view.channelListView

        initList(mListData)
        YoutubeAPIRequest(mListData, queryViewModel.getYoutubeQueryUrl(),
                adapter as RecyclerView.Adapter<RecyclerView.ViewHolder> ).execute()

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        queryViewModel =  ViewModelProviders.of(activity!!).get(QueryUrlViewModel::class.java)
//        YoutubeAPIRequest(mListData, queryViewModel.getYoutubeQueryUrl(), mListView.adapter).execute()
    }

    private fun initList(listData : ArrayList<ChannelModel>) {

        adapter= ChannelPostAdapter(activity as Context, listData){
            channelModel ->

            val intent = Intent(activity, VideoPlayActivity::class.java)
            Log.d("initList", channelModel.toString())
            intent.putExtra(CHANNEL_MODEL, channelModel)
            startActivity(intent)
        }

        mListView.adapter = adapter
    }

    fun queryRequest(queryUrl: String){
        Log.d("QueryRequest", "Url: $queryUrl")
        YoutubeAPIRequest(mListData, queryUrl, mListView.adapter).execute()
    }

    companion object {
        fun newInstance() = ChannelFragment()
    }

}


