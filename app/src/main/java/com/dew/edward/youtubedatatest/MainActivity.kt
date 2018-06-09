package com.dew.edward.youtubedatatest

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.util.Log
import com.dew.edward.youtubedatatest.adapters.PageAdapter
import com.dew.edward.youtubedatatest.fragments.ChannelFragment
import com.dew.edward.youtubedatatest.modules.QUERY
import com.dew.edward.youtubedatatest.modules.TAB_TOTAL
import com.dew.edward.youtubedatatest.viewmodels.QueryUrlViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var adapter: PageAdapter

    private val queryViewModel by lazy {
        ViewModelProviders.of(this).get(QueryUrlViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // setup tabs titles
        for (i in 0 until TAB_TOTAL) {
            tabLayout.addTab(tabLayout.newTab())
        }

        // setup the view pager
        adapter = PageAdapter(supportFragmentManager, tabLayout.tabCount)
        viewPager.adapter = PageAdapter(supportFragmentManager, tabLayout.tabCount)
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.setupWithViewPager(viewPager)

        searchViewMain.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
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

                searchViewMain.onActionViewCollapsed()

//                Log.d(QUERY, "user input query: $queryString")
                queryViewModel.query = queryString

//                val frag: ChannelFragment = adapter.getItem(viewPager.currentItem) as ChannelFragment
//                Log.e("FRAGMENT", frag.toString())
//                frag.queryRequest(queryViewModel.getYoutubeQueryUrl())
                viewPager.adapter?.notifyDataSetChanged()
               

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }
        })

    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.viewPager, fragment)
                .commit()
    }


}
