package com.zoe.weshare.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cards(

    val id: String,
    val title: String,
    val createdTime: Long,
    val postType: Int,
    val image: String,
    val locationName: String,

) : Parcelable
