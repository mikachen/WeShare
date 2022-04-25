package com.zoe.weshare.data

data class PostLog(
    var id: String ="",
    var postDocId: String = "",
    var logType: Int = -1 ,
    var operatorUid : String, //uid
    var logMsg: String ="",
    var createdTime: Long = -1,
    )
