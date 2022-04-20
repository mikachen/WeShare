package com.zoe.weshare.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfile(
    val email: String = "",
    val uid: String = "",
    val image: String = "",
    val lastLoginTime: Long = -1,
    val name: String = "",
    val token: String = "",
    val follower: List<String>? = null,
    val following: List<String>? = null,
    val introMsg: String=""

    ):Parcelable
