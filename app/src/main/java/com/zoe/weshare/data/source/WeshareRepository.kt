package com.zoe.weshare.data.source

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.zoe.weshare.data.*

interface WeShareRepository {
    suspend fun postNewEvent(event: EventPost): Result<String>
    suspend fun postNewGift(gift: GiftPost): Result<String>
    suspend fun getGifts(): Result<List<GiftPost>>
    suspend fun getEvents(): Result<List<EventPost>>

    fun getLiveEventDetail(docId: String): MutableLiveData<EventPost?>

    fun getLiveLogs(): MutableLiveData<List<PostLog>>

    fun getLiveMessages(docId: String): MutableLiveData<List<MessageItem>>

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

    suspend fun getUserLog(uid: String): Result<List<PostLog>>
    suspend fun getUserHistoryPosts(collection: String, uid: String): Result<List<GiftPost>>

    suspend fun saveLastMsgRecord(docId: String, message: Comment): Result<Boolean>
    suspend fun createNewChatRoom(newRoom: ChatRoom): Result<String>
    suspend fun saveLog(log: PostLog): Result<Boolean>

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
}
