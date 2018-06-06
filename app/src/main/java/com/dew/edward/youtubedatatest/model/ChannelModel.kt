package com.dew.edward.youtubedatatest.model

import android.os.Parcel
import android.os.Parcelable

/*
 * Created by Edward on 6/5/2018.
 */

data class ChannelModel(val title: String ="", val channelTitle: String ="",
                   val publishedAt: String = "", val thumbNail: String = "",
                   val videoId: String): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(channelTitle)
        parcel.writeString(publishedAt)
        parcel.writeString(thumbNail)
        parcel.writeString(videoId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChannelModel> {
        override fun createFromParcel(parcel: Parcel): ChannelModel {
            return ChannelModel(parcel)
        }

        override fun newArray(size: Int): Array<ChannelModel?> {
            return arrayOfNulls(size)
        }
    }
}