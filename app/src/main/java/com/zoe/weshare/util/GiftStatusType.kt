package com.zoe.weshare.util

enum class GiftStatusType(val code: Int, val tag: String) {
    OPENING(0,"開放中"),
    CLOSED(1,"已結案"),
    ABANDONED(2, "已下架"),
}