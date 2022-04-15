package com.zoe.weshare.data

sealed class MessageItem {

    abstract val id: String?

    data class OnSendSide(val message: Comment? = null) : MessageItem() {
        override val id: String? = message?.id
    }
    data class OnReceiveSide(val message: Comment? = null) : MessageItem() {
        override val id: String? = message?.id

    }
}