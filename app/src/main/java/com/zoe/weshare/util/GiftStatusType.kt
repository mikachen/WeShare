package com.zoe.weshare.util

import com.zoe.weshare.R
import com.zoe.weshare.util.Util.getString

enum class GiftStatusType(val code: Int, val tag: String) {
    OPENING(0, getString(R.string.gift_status_opening)),
    CLOSED(1, getString(R.string.gift_status_closed)),
    ABANDONED(2, getString(R.string.gift_status_abandoned))
}
