package com.zoe.weshare.util

import com.zoe.weshare.R
import com.zoe.weshare.util.Util.getString


enum class CurrentFragmentType(val value: String) {
    HOME(getString(R.string.home)),
    MAP(getString(R.string.map)),
    MESSAGE(getString(R.string.message)),
    PROFILE(getString(R.string.profile)),
    POSTEVENT(getString(R.string.post_event)),
    POSTGIFT(getString(R.string.post_gift))
}