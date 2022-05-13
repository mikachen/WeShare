package com.zoe.weshare.util

enum class LogType(val value: Int, val contribution: Int) {
    POST_GIFT(0,20),
    POST_EVENT(1,20),
    SEND_GIFT(2,100),
    EVENT_CHECK_IN(3,100), // send notification to author only
    EVENT_STARTED(4,0),
    EVENT_ENDED(5,0),

    COMMENT_EVENT(6,1),
    ABANDONED_GIFT(7,0),
    ATTEND_EVENT(8,5),
    VOLUNTEER_EVENT(9,5), // send notification to author only
    FOLLOWING(10,0),
    REQUEST_GIFT(11,1), // send notification to author only
    EVENT_GOT_FORCE_ENDED(12,0)
}
