package com.zoe.weshare.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatRoom (
    val id: String ="",
    val title: String = "",
    val lastMsg: String = "",
    val lastMsgSentTime: Long = -1,
    val participants: Participant? = null
):Parcelable


@Parcelize
data class Participant(
    val uid: Boolean = true,
):Parcelable