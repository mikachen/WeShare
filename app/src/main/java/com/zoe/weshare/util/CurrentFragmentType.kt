package com.zoe.weshare.util

import com.zoe.weshare.R
import com.zoe.weshare.util.Util.getString

enum class CurrentFragmentType(val value: String) {
    HOME(getString(R.string.home)),
    MAP(getString(R.string.map)),
    ROOMLIST(getString(R.string.message)),
    CHATROOM(""), // 預期後面assign target user's name
    PROFILE(getString(R.string.profile)),
    POSTEVENT(getString(R.string.post_event)),
    POSTGIFT(getString(R.string.post_gift)),
    GIFTDETAIL("贈品詳情"),
    EVENTDETAIL("活動詳情"),
    SEARCHLOCATION("請選擇地點"),
    GIFTMANAGE("贈品管理"),
    NOTIFICATION("通知"),
    LOGIN("登入"),
    EDITPROFILE("會員資料更新")


}
