package com.zoe.weshare.data

data class ViolationReport(
    var id: String = "",
    var targetUid: String = "",
    var operatorUid: String = "",
    var reason: String = "",
    var sort:String ="",
    var createdTime: Long = -1
)