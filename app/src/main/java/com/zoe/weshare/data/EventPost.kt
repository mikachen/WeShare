package com.zoe.weshare.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EventPost(
    var id: String = "",
    var author: Author? = null,
    var title: String = "",
    var sort: String = "",
    var volunteerNeeds: Int = -1,
    var description: String = "",
    var image: String = "",
    var createdTime: Long = -1,
    var location: PostLocation? = null,
    var whoLiked: List<String>? = null
) : Parcelable
