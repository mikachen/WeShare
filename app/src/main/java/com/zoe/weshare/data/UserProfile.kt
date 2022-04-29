package com.zoe.weshare.data


data class UserProfile(
    var uid: String = "",
    var image: String = "",
    var name: String = "",
    var follower: List<String> = emptyList(),
    var following: List<String> = emptyList(),
    var introMsg: String = "",
)
