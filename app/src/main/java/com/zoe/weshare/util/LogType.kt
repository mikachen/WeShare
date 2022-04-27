package com.zoe.weshare.util

enum class LogType(val value: Int) {
    POST_GIFT(0),
    POST_EVENT(1),
    REQUEST_GIFT(2),
    SEND_GIFT(3),
    ATTEND_EVENT(4),
    VOLUNTEER_EVENT(5),
    COMMENT_EVENT(6),
    ABANDONED_GIFT(7),
    EVENT_STARTED(8),
    EVENT_ENDED(9)
}
