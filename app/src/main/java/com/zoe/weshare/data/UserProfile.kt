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
    var contribution: Contribution? = null,
    var blackList: MutableList<String> = mutableListOf(),
) : Parcelable

@Parcelize
data class Contribution(
    val totalContribution: Int = 0,
    var giftPostsCount: Int = 0,
    var eventPostsCount: Int = 0,
    var sendGiftsCount: Int = 0,
    var attendeesCount: Int = 0,
    var volunteerCount: Int = 0,
    var checkInCount: Int = 0,
    var commentsCount: Int = 0,
    var requestGiftsCount: Int = 0,
) : Parcelable
