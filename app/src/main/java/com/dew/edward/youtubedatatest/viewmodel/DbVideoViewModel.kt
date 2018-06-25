package com.dew.edward.youtubedatatest.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.dew.edward.youtubedatatest.repository.DbVideoModelRepository


/**
 * Created by Edward on 6/25/2018.
 */
class DbVideoViewModel(context: Context): ViewModel() {
    val repository = DbVideoModelRepository(context, true) // using inMemoryDb for now

    private val queryString = MutableLiveData<String>()
    private val searchResult =
            Transformations.map(queryString) {
                repository.postsOfSearchYoutube(it, 30)
            }
    val posts = Transformations.switchMap(searchResult) { it.pagedList }!!
    val networkState = Transformations.switchMap(searchResult) { it.networkState }!!
    val refreshState = Transformations.switchMap(searchResult) { it.refreshState }!!

    fun refresh() {
        searchResult.value?.refresh?.invoke()
    }

    fun showSearchQuery(searchQuery: String): Boolean =
            if (queryString.value == searchQuery) false
            else {
                queryString.value = searchQuery
                true
            }

    fun retry(){
        val listing = searchResult?.value
        listing?.retry?.invoke()
    }

    fun currentQuery(): String? = queryString.value

}