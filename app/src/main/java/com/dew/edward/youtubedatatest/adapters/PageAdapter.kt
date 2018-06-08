package com.dew.edward.youtubedatatest.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.dew.edward.youtubedatatest.fragments.ChannelFragment
import com.dew.edward.youtubedatatest.fragments.LiveFragment
import com.dew.edward.youtubedatatest.fragments.PlayListFragment

/*
 * Created by Edward on 6/5/2018.
 */

class PageAdapter(fragmentManager: FragmentManager, val numberOfTabs: Int) :
        FragmentStatePagerAdapter(fragmentManager){

    override fun getItem(position: Int): Fragment {
        return when (position){
            0 -> ChannelFragment()
            1 -> PlayListFragment()
            2 -> LiveFragment()
            else -> ChannelFragment()
        }
    }

    override fun getCount(): Int = numberOfTabs
    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Trailers"
            1 -> "Playlist"
            2 -> "Live"
            else -> null
        }
    }
}