package com.zoe.weshare.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostLog(
    var id: String = "",
    var postDocId: String = "",
    var logType: Int = -1,
    var operatorUid: String = "", // uid
    var logMsg: String = "",
    var createdTime: Long = -1
) : Parcelable
