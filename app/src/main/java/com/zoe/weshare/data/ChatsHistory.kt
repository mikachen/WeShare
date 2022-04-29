package com.zoe.weshare.data

import com.zoe.weshare.util.UserManager
import com.zoe.weshare.util.UserManager.weShareUser

// TODO 如果要分日期就先分類好 data list 執行以下

data class ChatsHistory(
    val msgList: List<Comment>? = null
) {
    fun toMessageItem(): List<MessageItem> {
        val items = mutableListOf<MessageItem>()

        msgList?.let {
            for (element in it) {
                if (element.uid == weShareUser!!.uid) {
                    items.add(MessageItem.OnSendSide(element))
                } else {
                    items.add(MessageItem.OnReceiveSide(element))
                }
            }
        }
        return items
    }
}
