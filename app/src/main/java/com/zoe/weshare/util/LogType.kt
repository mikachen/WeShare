package com.zoe.weshare.util

enum class LogType(val value: Int) {
    POST_GIFT(0),
    POST_EVENT(1),
    SEND_GIFT(2),
    EVENT_CHECK_IN(3), // send notification to author only
    EVENT_STARTED(4),
    EVENT_ENDED(5),

    COMMENT_EVENT(6),
    ABANDONED_GIFT(7),
    ATTEND_EVENT(8),
    VOLUNTEER_EVENT(9), // send notification to author only
    FOLLOWING(10),
    REQUEST_GIFT(11), // send notification to author only
    EVENT_GOT_FORCE_ENDED(12)
}
