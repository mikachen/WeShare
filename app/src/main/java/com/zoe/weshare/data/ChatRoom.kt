package com.zoe.weshare.data


class ChatRoom (
    val id: String ="",
    val title: String = "",
    val lastMsg: String = "",
    val lastMsgSentTime: Long = -1,
    val participantsUid: List<String>? = null
)