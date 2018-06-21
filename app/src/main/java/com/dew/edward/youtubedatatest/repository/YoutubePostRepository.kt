package com.dew.edward.youtubedatatest.repository

import com.dew.edward.youtubedatatest.model.VideoModel

interface YoutubePostRepository {
    fun postsOfSearchYoutube(subYoutube: String, pageSize: Int): Listing<VideoModel>

    enum class Type {
        IN_MEMORY,
        DB
    }

}