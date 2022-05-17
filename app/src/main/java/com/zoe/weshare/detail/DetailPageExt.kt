package com.zoe.weshare.detail

import com.zoe.weshare.data.Comment
import com.zoe.weshare.util.UserManager.weShareUser

fun getLikeCounts(list: List<String>): Int {
    return list.size
}

fun hasUserRequestedBefore(list: List<Comment>): Boolean {
    return list.none { it.uid == weShareUser!!.uid }
}

fun hasUserLikedBefore(list: List<String>): Boolean {
    return list.contains(weShareUser!!.uid)
}

fun isUserThePostAuthor(authorUid:String):Boolean{
    return authorUid == weShareUser!!.uid
}