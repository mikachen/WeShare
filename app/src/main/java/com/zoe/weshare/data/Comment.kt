package com.zoe.weshare.data

data class Comment(
    var id: String = "",
    var uid: String = "",
    var content: String = "",
    var createdTime: Long = -1,
    var whoLiked: List<String>? = null
)
