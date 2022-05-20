package com.zoe.weshare.data

import android.os.Parcelable
import com.zoe.weshare.util.GiftStatusType
import kotlinx.parcelize.Parcelize

@Parcelize
data class GiftPost(
    var id: String = "",
    var author: UserInfo = UserInfo(),
    var title: String = "",
    var sort: String = "",
    var condition: String = "",
    var description: String = "",
    var image: String = "",
    var createdTime: Long = -1,
    var location: PostLocation = PostLocation(),
    var status: Int = GiftStatusType.OPENING.code,
    var whoLiked: List<String> = emptyList(),
    var whoGetGift: String = ""
) : Parcelable
