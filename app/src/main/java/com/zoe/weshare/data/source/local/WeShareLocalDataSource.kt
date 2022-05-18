package com.zoe.weshare.data.source.local

import android.content.Context
import android.net.Uri
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

    override fun getLiveRoomLists(uid: String): MutableLiveData<List<ChatRoom>> {
        TODO("Not yet implemented")
    }

    override suspend fun removeDocument(collection: String, docId: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun setLastMsgReadUser(docId: String, uidList: List<String>): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllGifts(): Result<List<GiftPost>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllEvents(): Result<List<EventPost>> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserAllGiftsPosts(uid: String): Result<List<GiftPost>> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserAllEventsPosts(uid: String): Result<List<EventPost>> {
        TODO("Not yet implemented")
    }

    override suspend fun sendNotifications(targetUid: String, log: OperationLog): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override fun getLiveNotifications(uid: String): MutableLiveData<List<OperationLog>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override fun getLiveEventDetail(docId: String): MutableLiveData<EventPost?> {
        TODO("Not yet implemented")
    }

    override suspend fun sendViolationReport(report: ViolationReport): Result<String> {
        TODO("Not yet implemented")
    }

    override fun getLiveGiftDetail(docId: String): MutableLiveData<GiftPost?> {
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

    override suspend fun updateSubCollectionFieldValue(
        collection: String,
        docId: String,
        subCollection: String,
        subDocId: String,
        field: String,
        value: FieldValue,
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun readNotification(
        uid: String,
        docId: String,
        read: Boolean,
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadImage(imageUri: Uri): Result<String> {
        TODO("Not yet implemented")
    }

    override fun getLiveLogs(): MutableLiveData<List<OperationLog>> {
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

    override suspend fun saveLog(log: OperationLog): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserLog(uid: String): Result<List<OperationLog>> {
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

    override suspend fun updateUserContribution(
        uid: String,
        contribution: Contribution,
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getHeroRanking(): Result<List<UserProfile>> {
        TODO("Not yet implemented")
    }
}
