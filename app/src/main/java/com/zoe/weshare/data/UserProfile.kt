package com.zoe.weshare.data

data class UserProfile(
    val email: String = "",
    val uid: String = "",
    val image: String = "",
    val lastLoginTime: Long = -1,
    val name: String = "",
    val token: String = "",
)
