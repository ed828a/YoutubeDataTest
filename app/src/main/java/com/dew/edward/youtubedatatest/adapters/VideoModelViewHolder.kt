package com.dew.edward.youtubedatatest.adapters

import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dew.edward.youtubedatatest.R
import com.dew.edward.youtubedatatest.VideoPlayActivity
import com.dew.edward.youtubedatatest.model.VideoModel
import com.dew.edward.youtubedatatest.modules.CHANNEL_MODEL
import com.dew.edward.youtubedatatest.modules.GlideRequests
import kotlinx.android.synthetic.main.channel_post_cell.view.*


/**
 * Created by Edward on 6/21/2018.
 */
class VideoModelViewHolder(view: View, private val glide: GlideRequests): RecyclerView.ViewHolder(view) {
    private val textViewTitle = view.textViewTitle
    private val textViewDesc = view.textViewChannelTitle
    private val textViewDate = view.textViewDate
    private val imageViewThumb = view.imageViewThumb
    private val videoModel: VideoModel? = null
    init {
        view.setOnClickListener {
            videoModel?.let {
                val intent = Intent(view.context, VideoPlayActivity::class.java)
                Log.d("setOnClickListener", it.toString())
                intent.putExtra(CHANNEL_MODEL, it)
                startActivity(view.context, intent, null)
            }
        }
    }

    fun bind(videoModel: VideoModel){
        textViewTitle.text = videoModel.title
        textViewDate.text = videoModel.date
        glide.load(videoModel.thumbnail).centerCrop().into(imageViewThumb)
    }

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests): VideoModelViewHolder{
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.channel_post_cell, parent, false)
            return VideoModelViewHolder(view, glide)
        }
    }
}