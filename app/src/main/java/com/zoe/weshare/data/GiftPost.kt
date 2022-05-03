package com.zoe.weshare.data

import android.os.Parcelable
import com.zoe.weshare.util.GiftStatusType
import kotlinx.parcelize.Parcelize

@Parcelize
data class GiftPost(
    var id: String = "",
    var author: UserInfo? = null,
    var title: String = "",
    var sort: String = "",
    var condition: String = "",
    var description: String = "",
    var image: String = "https://hips.hearstapps.com/hmg-prod.s3.amazonaws.com/images/vdaygifts-1641495726.png",
    var createdTime: Long = -1,
    var location: PostLocation? = null,
    var status: Int = GiftStatusType.OPENING.code,
    var whoLiked: List<String> = emptyList(),
    var whoGetGift: String = ""
) : Parcelable
