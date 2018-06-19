package com.dew.edward.youtubedatatest.adapters

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dew.edward.youtubedatatest.R
import com.dew.edward.youtubedatatest.model.ChannelModel
import com.dew.edward.youtubedatatest.modules.GlideApp
import kotlinx.android.synthetic.main.channel_post_cell.view.*


/*
 * Created by Edward on 6/5/2018.
 */

class MainPostAdapter(val context: Context, val channelList: List<ChannelModel>,
                         val listener: (ChannelModel) -> Unit) :
        RecyclerView.Adapter<MainPostAdapter.MainPostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainPostViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.channel_post_cell, parent, false)

        return MainPostViewHolder(view)
    }

    override fun getItemCount(): Int = channelList.count()

    override fun onBindViewHolder(holder: MainPostViewHolder, position: Int) {
        Log.e("OnBindVuewHolder", "This is position $position")
        val channel = channelList[position]
        with(holder){
            textViewTitle?.text = channel.title
            textViewDesc?.text = channel.channelTitle
            textViewDate?.text = channel.publishedAt
            GlideApp.with(context).load(Uri.parse(channel.thumbNail)).into(imageViewThumb!!)
            setOnItemClick(channel)
        }

    }

    inner class MainPostViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val textViewTitle = itemView?.textViewTitle
        val textViewDesc = itemView?.textViewChannelTitle
        val textViewDate = itemView?.textViewDate
        val imageViewThumb = itemView?.imageViewThumb

        fun setOnItemClick(item: ChannelModel){
            itemView.setOnClickListener{
                listener(item)
            }
        }
    }
}