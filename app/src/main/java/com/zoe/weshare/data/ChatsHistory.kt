package com.zoe.weshare.data

//TODO 如果要分日期就先分類好 data list 執行以下

data class ChatsHistory(
    val msgList: List<Comment>? = null,
) {
    fun toMessageItem(): List<MessageItem> {
        val items = mutableListOf<MessageItem>()

        msgList?.let {
            for (element in it) {
                if (element.uid == "zoe1018") {
                    items.add(MessageItem.OnSendSide(element))
                } else {
                    items.add(MessageItem.OnReceiveSide(element))
                }
            }
        }
            return items
    }
}