package com.zoe.weshare.data

data class GiftPost(
    var id: String = "", //unique = doc id
    var author: Author? = null,
    var title: String = "",
    var sort: String = "",
    var condidtion: String = "",
    var description: String = "",
    var image: String = "",
    var createdTime: Long = -1,
    var quantity: Int = -1,
//    var location: GeoPoint, //N,E
)