package com.dew.edward.youtubedatatest.controllers

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.dew.edward.youtubedatatest.R
import com.dew.edward.youtubedatatest.VideoPlayActivity
import com.dew.edward.youtubedatatest.adapters.VideoModelAdapter
import com.dew.edward.youtubedatatest.model.ChannelModel
import com.dew.edward.youtubedatatest.model.VideoModel
import com.dew.edward.youtubedatatest.modules.CHANNEL_MODEL
import com.dew.edward.youtubedatatest.modules.GlideApp
import com.dew.edward.youtubedatatest.repository.NetworkState
import com.dew.edward.youtubedatatest.util.BROADCAST_DATA_CHANGED
import com.dew.edward.youtubedatatest.util.VideoApp
import com.dew.edward.youtubedatatest.viewmodel.DbVideoViewModel
import kotlinx.android.synthetic.main.activity_search.*
import java.util.concurrent.Executors

class DbAndSearchActivity : AppCompatActivity() {

    companion object {
        const val KEY_QUERY = "query"
        const val DEFAULT_QUERY = "trump"
    }

    private lateinit var videoViewModel: DbVideoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        videoViewModel = getViewModel()
        initRecyclerView()

        initSwipeToRefresh()
        initSearch()
        val query = savedInstanceState?.getString(KEY_QUERY) ?: DEFAULT_QUERY
        videoViewModel.showSearchQuery(query)
        VideoApp.localBroadcastManager.registerReceiver(dataChangedReceiver, IntentFilter(BROADCAST_DATA_CHANGED))
    }

    private fun getViewModel(): DbVideoViewModel =
            ViewModelProviders.of(this, object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                        DbVideoViewModel(this@DbAndSearchActivity) as T
            })[DbVideoViewModel::class.java]

    private fun initRecyclerView() {
        val glide = GlideApp.with(this)
        val adapter = VideoModelAdapter(glide, { videoViewModel.retry() }, {
            val intent = Intent(this@DbAndSearchActivity, VideoPlayActivity::class.java)
            ChannelModel(it.title, "", it.date, it.thumbnail, it.videoId)
            intent.putExtra(CHANNEL_MODEL,
                    ChannelModel(it.title, "", it.date, it.thumbnail, it.videoId))
            startActivity(intent)
        })

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            videoList.layoutManager = GridLayoutManager(this, 2)
        } else {
            videoList.layoutManager = LinearLayoutManager(this)
        }

        videoList.adapter = adapter
        videoList.setHasFixedSize(true)
        videoViewModel.posts.observe(this, Observer<PagedList<VideoModel>> {
            adapter.submitList(it)
        })
        videoViewModel.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })


    }

    private fun initSwipeToRefresh() {
        videoViewModel.refreshState.observe(this, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener {
            videoViewModel.refresh()
        }
    }

    private fun initSearch() {
        input.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updateQueryStringFromInput()
                hideKeyboard()
                true
            } else {
                false
            }
        }

        input.setOnKeyListener { view, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateQueryStringFromInput()
                hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    private fun updateQueryStringFromInput() {
        input.text.trim().toString().let {
            if (it.isNotEmpty()) {
                if (videoViewModel.showSearchQuery(it)) {
                    videoList.scrollToPosition(0)
                    (videoList.adapter as? VideoModelAdapter)?.submitList(null)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString(KEY_QUERY, videoViewModel.currentQuery())
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    private val dataChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            videoList.scrollToPosition(0)
        }
    }

    override fun onDestroy() {
        Executors.newSingleThreadExecutor().execute {
            videoViewModel.repository.db.videoDao().deleteVideosByQuery()
        }

        VideoApp.localBroadcastManager.unregisterReceiver(dataChangedReceiver)
        super.onDestroy()
    }
}
