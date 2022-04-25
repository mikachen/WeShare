package com.zoe.weshare.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfile(
    val uid: String = "",
    val image: String = "",
    val name: String = "",
    val token: String = "",
    val follower: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val introMsg: String = ""
) : Parcelable
