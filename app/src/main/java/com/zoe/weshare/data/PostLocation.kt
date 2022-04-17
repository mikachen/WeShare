package com.zoe.weshare.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PostLocation(
    val locationName: String = "",
    val latitude: String = "",
    val longitude: String = ""
) : Parcelable
