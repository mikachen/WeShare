package com.zoe.weshare.data.source.local

import android.content.Context
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareDataSource

/*
 * Concrete implementation of a WeShare source as a db.
 */
class WeShareLocalDataSource(val context: Context) : WeShareDataSource {

    override suspend fun postNewEvent(event: EventPost): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun postNewGift(gift: GiftPost): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getGifts(): Result<List<GiftPost>> {
        TODO("Not yet implemented")
    }

    override suspend fun getEvents(): Result<List<EventPost>> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserInfo(uid: String): Result<UserProfile> {
        TODO("Not yet implemented")
    }

    override suspend fun getGiftAskForComments(docId: String): Result<List<Comment>> {
        TODO("Not yet implemented")
    }

    override suspend fun askForGift(docId: String, comment: Comment): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun sendEventComment(docId: String, comment: Comment): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getEventComments(docId: String): Result<List<Comment>> {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(docId: String, comment: Comment): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getChatsHistory(docId: String): Result<List<MessageItem>>{
        TODO("Not yet implemented")
    }
}
