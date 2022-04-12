package com.zoe.weshare.data

data class EventPost(
    var id: String = "", // unique = doc id
    var author: Author? = null,
    var title: String = "",
    var sort: String = "",
//    var coEditor: String, //共編人
    var volunteerNeeds: Int = -1,
    var description: String = "",
    var image: String = "",
    var createdTime: Long = -1,
//    var location: GeoPoint, //N,E
)
