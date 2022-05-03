package com.zoe.weshare.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OperationLog(
    var id: String = "",
    var postDocId: String = "",
    var logType: Int = -1,
    var operatorUid: String = "",
    var logMsg: String = "",
    var createdTime: Long = -1,
    var isRead: Boolean = false
) : Parcelable
