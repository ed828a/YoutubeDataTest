package com.dew.edward.youtubedatatest.viewmodels

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class QueryViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QueryUrlViewModel::class.java)){
            return QueryUrlViewModel() as T
        }
        throw IllegalArgumentException("unknown ViewModel class")
    }
}