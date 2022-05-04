package com.zoe.weshare.util

enum class LogType(val value: Int) {
    POST_GIFT(0),
    POST_EVENT(1),
    REQUEST_GIFT(2),  // send notification to author only
    SEND_GIFT(3),
    ATTEND_EVENT(4),
    VOLUNTEER_EVENT(5),  // send notification to author only
    COMMENT_EVENT(6),
    ABANDONED_GIFT(7),
    EVENT_STARTED(8),
    EVENT_ENDED(9),
    FOLLOWING(10)
}
