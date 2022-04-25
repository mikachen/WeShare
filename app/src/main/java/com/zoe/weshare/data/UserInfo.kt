package com.zoe.weshare.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserInfo(
    val uid: String = "",
    val name: String = "",
    val image: String = ""
) : Parcelable
