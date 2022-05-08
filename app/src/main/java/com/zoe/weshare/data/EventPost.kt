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
    var volunteerNeeds: Int = -1,
    var description: String = "",
    var image: String = "https://images-platform.99static.com//uJrUDru1H4LsIwQs8XsP_XN4DDY=/fit-in/590x590/99designs-contests-attachments/28/28476/attachment_28476700",
    var location: PostLocation? = null,
    var endTime: Long = -1,
    var startTime: Long = -1,
    var createdTime: Long = -1,
    var roomId: String = "",
    var status: Int = EventStatusType.WAITING.code,
    var whoLiked: List<String> = emptyList(),
    var whoAttended: List<String> = emptyList(),
    var whoVolunteer: List<String> = emptyList(),
    var whoCheckedIn: List<String> = emptyList(),

    ) : Parcelable
