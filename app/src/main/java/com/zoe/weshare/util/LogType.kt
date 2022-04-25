package com.zoe.weshare.util

import com.zoe.weshare.util.Util.getString

enum class LogType (val value: Int) {
    POSTGIFT(0),
    POSTEVENT(1),
    REQUESTGIFT(2),
    GIVEOUTGIFT(3),
    ATTENDEVENT(4),
    REGIVOLUNTEER(5)
}