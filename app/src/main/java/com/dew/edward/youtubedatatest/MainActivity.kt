package com.dew.edward.youtubedatatest

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.util.Log
import com.dew.edward.youtubedatatest.adapters.PageAdapter
import com.dew.edward.youtubedatatest.modules.QUERY
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // setup tabs titles
        tabLayout.addTab(tabLayout.newTab().setText("Channel"))
        tabLayout.addTab(tabLayout.newTab().setText("PlayList"))
        tabLayout.addTab(tabLayout.newTab().setText("Live"))

        // setup the view pager
        viewPager.adapter = PageAdapter(supportFragmentManager, tabLayout.tabCount)
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.setCurrentItem(tab?.position!!)
            }

        })

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
                Log.d(QUERY, "user input query: $queryString")

                searchViewMain.onActionViewCollapsed()

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {


                return false
            }

        })

    }


}
