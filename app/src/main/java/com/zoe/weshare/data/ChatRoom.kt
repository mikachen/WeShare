package com.zoe.weshare.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatRoom(
    var id: String = "",
    var lastMsg: String = "",
    var lastMsgSentTime: Long = -1,
    var participants: List<String> = emptyList(),
    var usersInfo: List<UserInfo> = emptyList(),
    var type: Int = -1,
    var eventTitle: String = "",
    var eventImage: String = "",
    var lastMsgRead: List<String> = emptyList()
) : Parcelable
