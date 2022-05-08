package com.zoe.weshare.data.source

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.zoe.weshare.data.*

interface WeShareRepository {
    suspend fun postNewEvent(event: EventPost): Result<String>
    suspend fun postNewGift(gift: GiftPost): Result<String>
    suspend fun getAllGifts(): Result<List<GiftPost>>
    suspend fun getAllEvents(): Result<List<EventPost>>

    fun getLiveEventDetail(docId: String): MutableLiveData<EventPost?>

    fun getLiveLogs(): MutableLiveData<List<OperationLog>>

    fun getLiveMessages(docId: String): MutableLiveData<List<MessageItem>>

    fun getLiveNotifications(uid: String): MutableLiveData<List<OperationLog>>

    suspend fun getUserInfo(uid: String): Result<UserProfile?>
    suspend fun newUserRegister(user: UserProfile) : Result<Boolean>


    suspend fun sendComment(
        collection: String,
        docId: String,
        comment: Comment,
        subCollection: String
    ): Result<Boolean>

    suspend fun getAllComments(collection: String, docId: String, subCollection: String): Result<List<Comment>>

    suspend fun sendMessage(docId: String, comment: Comment): Result<Boolean>

    fun getLiveComments(
        collection: String,
        docId: String,
        subCollection: String,
    ): MutableLiveData<List<Comment>>


    suspend fun getUserChatRooms(uid: String): Result<List<ChatRoom>>

    suspend fun likeOnPostComment(
        collection: String,
        docId: String,
        subCollection: String,
        subDocId: String,
        uid: String
    ): Result<Boolean>

    suspend fun cancelLikeOnPostComment(
        collection: String,
        docId: String,
        subCollection: String,
        subDocId: String,
        uid: String
    ): Result<Boolean>

    suspend fun getUserLog(uid: String): Result<List<OperationLog>>
    suspend fun getUserAllGiftsPosts(collection: String, uid: String): Result<List<GiftPost>>

    suspend fun saveLastMsgRecord(docId: String, message: Comment): Result<Boolean>
    suspend fun createNewChatRoom(newRoom: ChatRoom): Result<String>
    suspend fun saveLog(log: OperationLog): Result<Boolean>

    suspend fun updateGiftStatus(docId: String, statusCode: Int, uid: String): Result<Boolean>


    suspend fun updateEventRoom(roomId: String, user: UserInfo): Result<Boolean>
    suspend fun getEventRoom(docId: String): Result<ChatRoom>

    suspend fun updateEventStatus(docId: String, code: Int): Result<Boolean>


    suspend fun updateFieldValue(
        collection: String,
        docId: String,
        field: String,
        value: FieldValue
    ): Result<Boolean>

    suspend fun updateSubCollectionFieldValue(
        collection: String,
        docId: String,
        subCollection: String,
        subDocId: String,
        field: String,
        value: FieldValue,
    ): Result<Boolean>

    suspend fun uploadImage(imageUri: Uri): Result<String>
    suspend fun updateUserProfile(profile: UserProfile): Result<Boolean>
    suspend fun sendNotifications(targetUid:String ,log: OperationLog): Result<Boolean>
    suspend fun readNotification(uid: String, docId: String, read: Boolean, ): Result<Boolean>
}
