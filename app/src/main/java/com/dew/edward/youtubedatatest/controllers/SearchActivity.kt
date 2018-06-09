package com.dew.edward.youtubedatatest.controllers

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.Toast
import com.dew.edward.youtubedatatest.R
import com.dew.edward.youtubedatatest.VideoPlayActivity
import com.dew.edward.youtubedatatest.adapters.ChannelPostAdapter
import com.dew.edward.youtubedatatest.model.ChannelModel
import com.dew.edward.youtubedatatest.modules.CHANNEL_MODEL
import com.dew.edward.youtubedatatest.repository.YoutubeAPIRequest
import com.dew.edward.youtubedatatest.viewmodels.QueryUrlViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {
    lateinit var adapter: ChannelPostAdapter
    private val queryViewModel by lazy {
        ViewModelProviders.of(this@SearchActivity).get(QueryUrlViewModel::class.java)
    }
    val mListData: ArrayList<ChannelModel> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

//        setSupportActionBar(toolbarSearch)

        adapter= ChannelPostAdapter(this@SearchActivity, mListData){
            channelModel ->
            val intent = Intent(this@SearchActivity, VideoPlayActivity::class.java)
            Log.d("initList", channelModel.toString())
            intent.putExtra(CHANNEL_MODEL, channelModel)
            startActivity(intent)
        }
        searchListView.adapter = adapter

        YoutubeAPIRequest(mListData, queryViewModel.getYoutubeQueryUrl(),
                searchListView.adapter).execute()

        searchAlone.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                var queryString: String =""
                val strings: List<String>? = query?.split(" ")
                if (strings != null && strings.isNotEmpty()){
                    for (position in 0 until strings.size){
                        queryString = if (position == 0){
                            strings[position]
                        } else {
                            "$queryString+${strings[position]}"
                        }
                    }
                }
                queryViewModel.query = queryString

                searchAlone.onActionViewCollapsed()
                Log.d("QUERY", "queryURL: ${queryViewModel.getYoutubeQueryUrl()}")
                YoutubeAPIRequest(mListData, queryViewModel.getYoutubeQueryUrl(),
                        searchListView.adapter).execute()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView: SearchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                var queryString: String =""
                val strings: List<String>? = query?.split(" ")
                if (strings != null && strings.isNotEmpty()){
                    for (position in 0 until strings.size){
                        queryString = if (position == 0){
                            strings[position]
                        } else {
                            "$queryString+${strings[position]}"
                        }
                    }
                }
                queryViewModel.query = queryString

                searchView.onActionViewCollapsed()
                Log.d("QUERY", "queryURL: ${queryViewModel.getYoutubeQueryUrl()}")
                YoutubeAPIRequest(mListData, queryViewModel.getYoutubeQueryUrl(),
                        searchListView.adapter).execute()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }
        })
        return true
    }
}
