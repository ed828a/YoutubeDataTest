package com.dew.edward.youtubedatatest.controllers

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.Toast
import com.dew.edward.youtubedatatest.R
import com.dew.edward.youtubedatatest.viewmodels.QueryUrlViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {
    lateinit var adapter: ArrayAdapter<String>
    private val queryViewModel by lazy {
        ViewModelProviders.of(this@SearchActivity).get(QueryUrlViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setSupportActionBar(toolbarSearch)

        adapter = ArrayAdapter(this@SearchActivity,
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.months_array))

        searchListView.adapter = adapter
        searchListView.setOnItemClickListener { parent, view, position, id ->
            Toast.makeText(this@SearchActivity,
                    parent.getItemAtPosition(position).toString(),
                    Toast.LENGTH_SHORT).show();
        }
        searchListView.emptyView


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

                searchView.onActionViewCollapsed()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })
        return true
    }
}
