package com.dew.edward.youtubedatatest.adapters

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dew.edward.youtubedatatest.R
import com.dew.edward.youtubedatatest.model.ChannelModel
import com.dew.edward.youtubedatatest.model.RelatedVideoModel
import com.dew.edward.youtubedatatest.modules.GlideApp
import kotlinx.android.synthetic.main.related_list_cell.view.*

/*
 * Created by Edward on 6/6/2018.
 */

class RelatedVideoAdapter(val context: Context,
                          val relatedVideoList: ArrayList<ChannelModel>,
                          val listener: (ChannelModel) -> Unit):
        RecyclerView.Adapter<RelatedVideoAdapter.RelatedVideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelatedVideoViewHolder {
        val view: View = LayoutInflater.from(context)
                .inflate(R.layout.related_list_cell, parent, false)

        return RelatedVideoViewHolder(view)
    }

    override fun getItemCount(): Int = relatedVideoList.count()

    override fun onBindViewHolder(holder: RelatedVideoViewHolder, position: Int) {
        val relatedVideo = relatedVideoList[position]

        with(holder){
            textTitle?.text = relatedVideo.title
            GlideApp.with(context).load(Uri.parse(relatedVideo.thumbNail)).into(imageThumb!!)
            setOnItemClickListener(relatedVideo)
        }


    }

    inner class RelatedVideoViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val textTitle= itemView?.textRelatedTitle
        val imageThumb = itemView?.imageViewRelated

        fun setOnItemClickListener(relatedVideo: ChannelModel){
            itemView.setOnClickListener { listener(relatedVideo) }
        }


    }
}