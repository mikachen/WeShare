package com.zoe.weshare.util

import com.zoe.weshare.R
import com.zoe.weshare.util.Util.getString

enum class EventStatusType(val code: Int, val tag: String) {
    WAITING(0, getString(R.string.event_status_waiting)),
    ONGOING(1, getString(R.string.event_status_opening)),
    ENDED(2, getString(R.string.event_status_ended)),
}
