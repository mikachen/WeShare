package com.zoe.weshare.data

import android.os.Parcelable
import com.zoe.weshare.util.EventStatusType
import kotlinx.parcelize.Parcelize

@Parcelize
data class EventPost(
    var id: String = "",
    var author: UserInfo? = null,
    var title: String = "",
    var sort: String = "",
    var date: Long = -1,
    var volunteerNeeds: Int = -1,
    var description: String = "",
    var image: String = "",
    var location: PostLocation? = null,
    var createdTime: Long = -1,
    var roomId: String = "",
    var status: Int = EventStatusType.WAITING.code,
    var whoLiked: List<String> = emptyList(),
    var whoAttended: List<String> = emptyList(),
    var whoVolunteer: List<String> = emptyList(),

    ) : Parcelable
