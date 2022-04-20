package com.zoe.weshare.data

import android.os.Parcelable
import com.zoe.weshare.util.EventStatusType
import kotlinx.parcelize.Parcelize

@Parcelize
data class EventPost(
    var id: String = "",
    var author: Author? = null,
    var title: String = "",
    var sort: String = "",
    var date: Long = -1,
    var volunteerNeeds: Int = -1,
    var description: String = "",
    var image: String = "",
    var location: PostLocation? = null,
    var createdTime: Long = -1,
    var status : Int = EventStatusType.WAITING.code,
    var whoLiked: List<String>? = null
) : Parcelable
