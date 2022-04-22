package com.zoe.weshare.util

enum class EventStatusType(val code: Int, val tag: String) {
    WAITING(0,"等待中"),
    ONGOING(1,"進行中"),
    ENDED(2, "已結束"),
}