package com.zoe.weshare.data.source

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.zoe.weshare.data.*

class DefaultWeShareRepository(
    private val remoteDataSource: WeShareDataSource,
    private val localDataSource: WeShareDataSource,
) : WeShareRepository {

    override suspend fun postNewEvent(event: EventPost): Result<String> {
        return remoteDataSource.postNewEvent(event)
    }

    override suspend fun postNewGift(gift: GiftPost): Result<String> {
        return remoteDataSource.postNewGift(gift)
    }

    override suspend fun getGifts(): Result<List<GiftPost>> {
        return remoteDataSource.getGifts()
    }

    override suspend fun getEvents(): Result<List<EventPost>> {
        return remoteDataSource.getEvents()
    }

    override fun getLiveEventDetail(docId: String): MutableLiveData<EventPost?>{
        return remoteDataSource.getLiveEventDetail(docId)
    }
    override fun getLiveLogs(): MutableLiveData<List<OperationLog>>{
        return remoteDataSource.getLiveLogs()
    }


    override fun getLiveMessages(docId: String): MutableLiveData<List<MessageItem>> {
        return remoteDataSource.getLiveMessages(docId)
    }

    override fun getLiveNotifications(uid: String): MutableLiveData<List<OperationLog>>{
        return remoteDataSource.getLiveNotifications(uid)
    }

    override suspend fun getUserInfo(uid: String): Result<UserProfile?> {
        return remoteDataSource.getUserInfo(uid)
    }

    override suspend fun newUserRegister(user: UserProfile) : Result<Boolean>{
        return remoteDataSource.newUserRegister(user)
    }

    override suspend fun sendComment(
        collection: String,
        docId: String,
        comment: Comment,
        subCollection: String,
    ): Result<Boolean> {
        return remoteDataSource.sendComment(collection, docId, comment, subCollection)
    }

    override suspend fun getAllComments(
        collection: String,
        docId: String,
        subCollection: String,
    ): Result<List<Comment>> {
        return remoteDataSource.getAllComments(collection, docId, subCollection)
    }

    override suspend fun getUserChatRooms(uid: String): Result<List<ChatRoom>> {
        return remoteDataSource.getUserChatRooms(uid)
    }

    override suspend fun createNewChatRoom(newRoom: ChatRoom): Result<String> {
        return remoteDataSource.createNewChatRoom(newRoom)
    }

    override suspend fun sendMessage(docId: String, comment: Comment): Result<Boolean> {
        return remoteDataSource.sendMessage(docId, comment)
    }



    override suspend fun likeOnPostComment(
        collection: String,
        docId: String,
        subCollection: String,
        subDocId: String,
        uid: String,
    ): Result<Boolean> {
        return remoteDataSource.likeOnPostComment(collection, docId, subCollection, subDocId, uid)
    }

    override suspend fun cancelLikeOnPostComment(
        collection: String,
        docId: String,
        subCollection: String,
        subDocId: String,
        uid: String,
    ): Result<Boolean> {
        return remoteDataSource.cancelLikeOnPostComment(
            collection,
            docId,
            subCollection,
            subDocId,
            uid
        )
    }

    override suspend fun saveLastMsgRecord(docId: String, message: Comment): Result<Boolean> {
        return remoteDataSource.saveLastMsgRecord(docId, message)
    }

    override suspend fun saveLog(log: OperationLog): Result<Boolean> {
        return remoteDataSource.saveLog(log)
    }

    override suspend fun getUserLog(uid: String): Result<List<OperationLog>> {
        return remoteDataSource.getUserLog(uid)
    }

    override suspend fun getUserHistoryPosts(
        collection: String,
        uid: String,
    ): Result<List<GiftPost>> {
        return remoteDataSource.getUserHistoryPosts(collection, uid)
    }

    override suspend fun updateGiftStatus(
        docId: String,
        statusCode: Int,
        uid: String,
    ): Result<Boolean> {
        return remoteDataSource.updateGiftStatus(docId, statusCode, uid)
    }

    override fun getLiveComments(
        collection: String,
        docId: String,
        subCollection: String,
    ): MutableLiveData<List<Comment>> {
        return remoteDataSource.getLiveComments(collection, docId, subCollection)
    }


    override suspend fun updateEventRoom(roomId: String, user: UserInfo): Result<Boolean> {
        return remoteDataSource.updateEventRoom(roomId, user)
    }

    override suspend fun updateEventStatus(docId: String, code: Int): Result<Boolean> {
        return remoteDataSource.updateEventStatus(docId, code)
    }

    override suspend fun getEventRoom(docId: String): Result<ChatRoom> {
        return remoteDataSource.getEventRoom(docId)
    }

    override suspend fun updateFieldValue(
        collection: String,
        docId: String,
        field: String,
        value: FieldValue
    ): Result<Boolean>{
        return remoteDataSource.updateFieldValue(collection, docId, field, value)
    }

    override suspend fun uploadImage(imageUri: Uri): Result<String> {
        return remoteDataSource.uploadImage(imageUri)
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Boolean> {
        return remoteDataSource.updateUserProfile(profile)
    }
    override suspend fun sendNotifications(targetUid:String ,log: OperationLog): Result<Boolean>{
        return remoteDataSource.sendNotifications(targetUid,log)
    }
}
