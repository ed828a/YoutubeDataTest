package com.dew.edward.youtubedatatest

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.dew.edward.youtubedatatest.adapters.MainPostAdapter
import com.dew.edward.youtubedatatest.controllers.ExoMediaActivity
import com.dew.edward.youtubedatatest.controllers.ExoPlayerActivity
import com.dew.edward.youtubedatatest.controllers.NewExoPlayerActivity
import com.dew.edward.youtubedatatest.model.ChannelModel
import com.dew.edward.youtubedatatest.modules.CHANNEL_MODEL
import com.dew.edward.youtubedatatest.repository.YoutubeAPIRequest
import com.dew.edward.youtubedatatest.viewmodels.QueryUrlViewModel
import kotlinx.android.synthetic.main.activity_main.*

/**
 * this one using naked OkHttp
 */
class MainActivity : AppCompatActivity() {

    private lateinit var adapter: MainPostAdapter
    private val queryViewModel by lazy {
        ViewModelProviders.of(this).get(QueryUrlViewModel::class.java)
    }
    var mListData: ArrayList<ChannelModel> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE){
            mainListView.layoutManager = GridLayoutManager(this, 2) as RecyclerView.LayoutManager?
        } else {
            mainListView.layoutManager = LinearLayoutManager(this)
        }


        initList(mListData)
        YoutubeAPIRequest(mListData, queryViewModel.getYoutubeQueryUrl(),
                adapter as RecyclerView.Adapter<RecyclerView.ViewHolder> ).execute()


        searchViewQuery.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
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

                searchViewQuery.onActionViewCollapsed()
//                hideKeyboard()
                Log.d("QUERY", "queryURL: ${queryViewModel.getYoutubeQueryUrl()}")
                YoutubeAPIRequest(mListData, queryViewModel.getYoutubeQueryUrl(),
                        mainListView.adapter).execute()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })


    }

    private fun initList(listData : ArrayList<ChannelModel>) {

        adapter= MainPostAdapter(this, listData){
            channelModel ->

//            val intent = Intent(this, VideoPlayActivity::class.java)
//            val intent = Intent(this, ExoMediaActivity::class.java)
//            val intent = Intent(this, ExoPlayerActivity::class.java)
            val intent = Intent(this, NewExoPlayerActivity::class.java)
            Log.d("initList", channelModel.toString())

            intent.putExtra(CHANNEL_MODEL, channelModel)
            startActivity(intent)
        }

        mainListView.adapter = adapter
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}
