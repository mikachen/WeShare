package com.zoe.weshare.util

import com.zoe.weshare.R
import com.zoe.weshare.util.Util.getString

enum class CurrentFragmentType(val value: String) {
    HOME(getString(R.string.home)),
    MAP(getString(R.string.map)),
    ROOMLIST(getString(R.string.message)),
    CHATROOM("聊天室標題？"),
    PROFILE(getString(R.string.profile)),
    POSTEVENT(getString(R.string.post_event)),
    POSTGIFT(getString(R.string.post_gift)),
    GIFTDETAIL("贈品詳情"),
    EVENTDETAIL("活動詳情"),
    SEARCHLOCATION("選擇地點")
}



enum class EventStatusType(val code: Int, val tag: String) {
    WAITING(0,"等待中"),
    ONGOING(1,"進行中"),
    ENDED(2, "已結束"),
}


enum class GiftStatusType(val code: Int, val tag: String) {
    OPENING(0,"開放中"),
    CLOSED(1,"已送出"),
    ABANDONED(2, "已下架"),
}


enum class ChatRoomType(val value: Int) {
    PRIVATE(0),
    MUTILCHAT(1)
}