package com.dew.edward.youtubedatatest.controllers

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import com.dew.edward.youtubedatatest.R
import com.dew.edward.youtubedatatest.adapters.VideoModelAdapter
import com.dew.edward.youtubedatatest.model.VideoModel
import com.dew.edward.youtubedatatest.modules.GlideApp
import com.dew.edward.youtubedatatest.repository.NetworkState
import com.dew.edward.youtubedatatest.repository.YoutubePostRepository
import com.dew.edward.youtubedatatest.util.ServiceLocator
import com.dew.edward.youtubedatatest.viewmodel.VideoViewModel
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {
    companion object {
        const val KEY_QUERY = "query"
        const val DEFAULT_QUERY = "trump"
        const val KEY_REPOSITORY_TYPE = "repository_type"
        fun intentFor(context: Context, type: YoutubePostRepository.Type): Intent{
            val intent = Intent(context, SearchActivity::class.java)
            intent.putExtra(KEY_REPOSITORY_TYPE, type.ordinal)
            return intent
        }
    }

    private lateinit var videoViewModel: VideoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        videoViewModel = getViewModel()
        initAdapter()
        initSwipeToRefresh()
        initSearch()
        val query = savedInstanceState?.getString(KEY_QUERY) ?: DEFAULT_QUERY
        videoViewModel.showSearchQuery(query)

    }

    private fun getViewModel(): VideoViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val typeParam = intent.getIntExtra(KEY_REPOSITORY_TYPE, 0)
                val repositoryType = YoutubePostRepository.Type.values()[typeParam]
                val repository = ServiceLocator.getInstance(this@SearchActivity).getRepository(repositoryType)
                return VideoViewModel(repository) as T
            }
        })[VideoViewModel::class.java]
    }

    private fun initAdapter(){
        val glide = GlideApp.with(this)
        val adapter = VideoModelAdapter(glide){ videoViewModel.retry() }
        videoList.adapter = adapter
        videoViewModel.posts.observe(this, Observer <PagedList<VideoModel>>{
            adapter.submitList(it)
        })
        videoViewModel.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })
    }

    private fun initSwipeToRefresh(){
        videoViewModel.refreshState.observe(this, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener {
            videoViewModel.refresh()
        }
    }

    private fun initSearch(){
        input.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO){
                updateQueryStringFromInput()
                true
            } else {
                false
            }
        }

        input.setOnKeyListener { view, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                updateQueryStringFromInput()
                true
            } else {
                false
            }
        }
    }

    private fun updateQueryStringFromInput(){
        input.text.trim().toString().let {
            if (it.isNotEmpty()){
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
}
