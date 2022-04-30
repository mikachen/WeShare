package com.zoe.weshare.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfile(
    var uid: String = "",
    var image: String = "",
    var name: String = "",
    var follower: List<String> = emptyList(),
    var following: List<String> = emptyList(),
    var introMsg: String = "",
):Parcelable
