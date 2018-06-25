package com.dew.edward.youtubedatatest.adapters

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dew.edward.youtubedatatest.R
import com.dew.edward.youtubedatatest.model.VideoModel
import com.dew.edward.youtubedatatest.modules.GlideRequests
import com.dew.edward.youtubedatatest.repository.NetworkState
import kotlinx.android.synthetic.main.channel_post_cell.view.*


/**
 * Created by Edward on 6/21/2018.
 */
class VideoModelAdapter(
        private val glide: GlideRequests,
        private val retryCallback: () -> Unit,
        val listener: (VideoModel) -> Unit) : PagedListAdapter<VideoModel, RecyclerView.ViewHolder>(COMPARATOR) {

    private var networkState: NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.channel_post_cell -> {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.channel_post_cell, parent, false)
                VideoModelViewHolder(view, glide)
            }
            R.layout.network_state_item -> NetworkStateItemViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.channel_post_cell -> {
                val item = getItem(position)
                if (item != null) {
                    (holder as VideoModelViewHolder).bind(item)
                    holder.setOnItemSelectedListener(getItem(position)!!)
                }
            }
            R.layout.network_state_item -> (holder as NetworkStateItemViewHolder).bindTo(networkState)
        }
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            R.layout.network_state_item
        } else {
            R.layout.channel_post_cell
        }
    }

    override fun getItemCount(): Int {
        return (super.getItemCount() + if (hasExtraRow()) 1 else 0)
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<VideoModel>() {
            override fun areItemsTheSame(oldItem: VideoModel?, newItem: VideoModel?): Boolean {
                return oldItem?.videoId == newItem?.videoId
            }

            override fun areContentsTheSame(oldItem: VideoModel?, newItem: VideoModel?): Boolean {
                return oldItem?.title == newItem?.title
            }

            override fun getChangePayload(oldItem: VideoModel, newItem: VideoModel): Any? {
                return if (oldItem.copy(title = newItem.title) == newItem) {
                    Any()
                } else {
                    null
                }
            }
        }

    }

    inner class VideoModelViewHolder(view: View,
                                     private val glide: GlideRequests) : RecyclerView.ViewHolder(view) {

        private val textViewTitle = view.textViewTitle
        private val textViewDesc = view.textViewChannelTitle
        private val textViewDate = view.textViewDate
        private val imageViewThumb = view.imageViewThumb

        fun setOnItemSelectedListener(videoModel: VideoModel) {
            itemView.setOnClickListener {
                listener(videoModel)
            }
        }


        fun bind(videoModel: VideoModel) {
            textViewTitle.text = videoModel.title
            textViewDate.text = videoModel.date
            glide.load(videoModel.thumbnail).centerCrop().into(imageViewThumb)
        }


        fun create(parent: ViewGroup, glide: GlideRequests, videoModel: VideoModel,
                   listener: (VideoModel) -> Unit): VideoModelViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.channel_post_cell, parent, false)
            return VideoModelViewHolder(view, glide)
        }

    }
}