package com.dew.edward.youtubedatatest.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.dew.edward.youtubedatatest.model.VideoModel


/**
 * Created by Edward on 6/24/2018.
 */
@Database(
        entities = [(VideoModel::class)],
        version = 1,
        exportSchema = false
)
abstract class VideoDb: RoomDatabase() {
    abstract fun videoDao(): VideoDao

    companion object {
        fun create(context: Context, useInMemory: Boolean): VideoDb {
            val dbBuilder = if (useInMemory) {
                Room.inMemoryDatabaseBuilder(context, VideoDb::class.java)
            } else {
                Room.databaseBuilder(context, VideoDb::class.java, "videos.db")
            }

            return dbBuilder
                    .fallbackToDestructiveMigration()  // because db is cache, deleting old data is fine
                    .build()
        }
    }
}