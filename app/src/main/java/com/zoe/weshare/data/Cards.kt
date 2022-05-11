package com.zoe.weshare.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cards(

    val id: String,
    val title: String,
    val description: String,
    val createdTime: Long,
    val eventTime: String,
    val postType: Int,
    val image: String,
    val postLocation: PostLocation?,
    val whoLiked: List<String>
) : Parcelable