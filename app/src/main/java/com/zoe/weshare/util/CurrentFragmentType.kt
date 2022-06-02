package com.zoe.weshare.util

import com.zoe.weshare.R
import com.zoe.weshare.util.Util.getString

enum class CurrentFragmentType(val value: String) {
    HOME(getString(R.string.home)),
    MAP(getString(R.string.map)),
    ROOMLIST(getString(R.string.message)),
    CHATROOM(""), // assign target user's name after
    PROFILE(getString(R.string.profile)),
    POSTEVENT(getString(R.string.post_event)),
    POSTGIFT(getString(R.string.post_gift)),
    GIFTDETAIL(getString(R.string.preview_gift_description_title)),
    EVENTDETAIL(getString(R.string.preview_event_description_title)),
    SEARCHLOCATION(getString(R.string.choose_location)),
    GIFTMANAGE(getString(R.string.gift_management)),
    EVENTMANAGE(getString(R.string.event_management)),
    NOTIFICATION(getString(R.string.notification)),
    LOGIN(getString(R.string.login)),
    EDITPROFILE(getString(R.string.edit_profile)),
    GIFTSBROWSE(getString(R.string.gift_browse)),
    EVENTSBROWSE(getString(R.string.event_browse)),
    EVENTCHECKIN(getString(R.string.event_check_in)),
    HERORANK(getString(R.string.hero_rank))
}
