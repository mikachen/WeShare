package com.zoe.weshare.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatRoom(
    var id: String = "",
    var title: String = "",
    var lastMsg: String = "",
    var lastMsgSentTime: Long = -1,
    var participants: List<String>? = null
) : Parcelable
