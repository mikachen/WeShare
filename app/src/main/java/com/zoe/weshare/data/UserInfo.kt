package com.zoe.weshare.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserInfo(
    var uid: String = "",
    var name: String = "",
    val image: String = ""
) : Parcelable
