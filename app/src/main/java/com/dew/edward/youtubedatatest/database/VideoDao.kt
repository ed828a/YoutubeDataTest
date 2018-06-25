package com.dew.edward.youtubedatatest.database

import android.arch.paging.DataSource
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.dew.edward.youtubedatatest.model.VideoModel


/**
 * Created by Edward on 6/23/2018.
 */

@Dao
interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(videos: List<VideoModel>)

    @Query("SELECT * FROM youtube_videos WHERE title LIKE :query ORDER BY indexInResponse ASC")
    fun getVideosByQuery(query: String): DataSource.Factory<Int, VideoModel>
//    @Query("SELECT * FROM youtube_videos ORDER BY indexInResponse ASC")
//    fun getVideosByQuery(): DataSource.Factory<Int, VideoModel>

//    @Query("DELETE FROM youtube_videos WHERE title LIKE :query")
//    fun deleteVideosByQuery(query: String)

    @Query("DELETE FROM youtube_videos")
    fun deleteVideosByQuery()

    //The MAX() function returns the largest value of the selected column.
    @Query("SELECT MAX(indexInResponse) + 1 FROM youtube_videos WHERE title LIKE :query")
    fun getNextIndexInVideo(query: String): Int

    @Query("SELECT * FROM youtube_videos ORDER BY indexInResponse ASC")
    fun dumpAll(): List<VideoModel>

    @Query("SELECT * FROM youtube_videos WHERE title LIKE :query ORDER BY indexInResponse ASC")
    fun dumpByQuery(query: String): List<VideoModel>
}