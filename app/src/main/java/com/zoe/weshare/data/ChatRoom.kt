package com.zoe.weshare.data


class ChatRoom (
    val id: String ="",
    val lastMsg: String = "",
    val lastMsgCreatedTime: Long = -1,
    val participantUid: List<String>? = null,
    val title: String = ""
)