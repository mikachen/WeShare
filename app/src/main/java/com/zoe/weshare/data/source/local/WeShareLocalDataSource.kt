package com.zoe.weshare.data.source.local

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareDataSource

class WeShareLocalDataSource(val context: Context) : WeShareDataSource {

    override suspend fun postNewEvent(event: EventPost): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun postNewGift(gift: GiftPost): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getGifts(): Result<List<GiftPost>> {
        TODO("Not yet implemented")
    }

    override suspend fun getEvents(): Result<List<EventPost>> {
        TODO("Not yet implemented")
    }

    override fun getLiveEventDetail(docId: String): MutableLiveData<EventPost?> {
        TODO("Not yet implemented")
    }

    override suspend fun updateFieldValue(
        collection: String,
        docId: String,
        field: String,
        value: FieldValue,
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override fun getLiveLogs(): MutableLiveData<List<PostLog>> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserInfo(uid: String): Result<UserProfile?> {
        TODO("Not yet implemented")
    }

    override suspend fun sendComment(
        collection: String,
        docId: String,
        comment: Comment,
        subCollection: String,
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun newUserRegister(user: UserProfile): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllComments(
        collection: String,
        docId: String,
        subCollection: String,
    ): Result<List<Comment>> {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(docId: String, comment: Comment): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserChatRooms(uid: String): Result<List<ChatRoom>> {
        TODO("Not yet implemented")
    }


    override suspend fun likeOnPostComment(
        collection: String,
        docId: String,
        subCollection: String,
        subDocId: String,
        uid: String,
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun cancelLikeOnPostComment(
        collection: String,
        docId: String,
        subCollection: String,
        subDocId: String,
        uid: String,
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun saveLastMsgRecord(
        docId: String,
        message: Comment,
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun createNewChatRoom(newRoom: ChatRoom): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun saveLog(log: PostLog): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserLog(uid: String): Result<List<PostLog>> {
        TODO("Not yet implemented")
    }

    override fun getLiveComments(
        collection: String,
        docId: String,
        subCollection: String,
    ): MutableLiveData<List<Comment>> {
        TODO("Not yet implemented")
    }

    override fun getLiveMessages(docId: String): MutableLiveData<List<MessageItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserHistoryPosts(
        collection: String,
        uid: String,
    ): Result<List<GiftPost>> {
        TODO("Not yet implemented")
    }


    override suspend fun updateEventRoom(roomId: String, user: UserInfo): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun updateEventStatus(docId: String, code: Int): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getEventRoom(docId: String): Result<ChatRoom> {
        TODO("Not yet implemented")
    }

    override suspend fun updateGiftStatus(
        docId: String,
        statusCode: Int,
        uid: String,
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }
}
