package com.dew.edward.youtubedatatest.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity(tableName = "youtube_videos", indices = [(Index(value = ["video_id"], unique = true))])
data class VideoModel(
        var title: String = "",
        var date: String = "",
        var thumbnail: String = "",
        @PrimaryKey
        @ColumnInfo(name = "video_id")
        var videoId: String = "",
        // to be consistent with changing backend order, we need to keep a data like this
        var indexInResponse: Int = -1) : Parcelable {



    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(date)
        parcel.writeString(thumbnail)
        parcel.writeString(videoId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VideoModel> {
        override fun createFromParcel(parcel: Parcel): VideoModel {
            return VideoModel(parcel)
        }

        override fun newArray(size: Int): Array<VideoModel?> {
            return arrayOfNulls(size)
        }
    }

}