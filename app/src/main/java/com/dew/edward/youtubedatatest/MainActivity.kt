package com.dew.edward.youtubedatatest

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.dew.edward.youtubedatatest.adapters.PageAdapter
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

    }


}
