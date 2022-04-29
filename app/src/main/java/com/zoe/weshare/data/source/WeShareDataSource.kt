package com.zoe.weshare.data.source

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.zoe.weshare.data.*

/**
 * Main entry point for accessing WeShare sources.
 */
interface WeShareDataSource {

    suspend fun postNewEvent(event: EventPost): Result<String>
    suspend fun postNewGift(gift: GiftPost): Result<String>

    suspend fun getGifts(): Result<List<GiftPost>>
    suspend fun getEvents(): Result<List<EventPost>>


    suspend fun getUserHistoryPosts(collection: String, uid: String): Result<List<GiftPost>>

    fun getLiveEventDetail(docId: String): MutableLiveData<EventPost?>

    fun getLiveLogs(): MutableLiveData<List<PostLog>>

    fun getLiveComments(
        collection: String,
        docId: String,
        subCollection: String,
    ): MutableLiveData<List<Comment>>

    fun getLiveMessages(docId: String): MutableLiveData<List<MessageItem>>

    suspend fun newUserRegister(user: UserProfile): Result<Boolean>
    suspend fun getUserInfo(uid: String): Result<UserProfile?>


    suspend fun sendComment(
        collection: String,
        docId: String,
        comment: Comment,
        subCollection: String,
    ): Result<Boolean>

    suspend fun getAllComments(
        collection: String,
        docId: String,
        subCollection: String,
    ): Result<List<Comment>>


    /** search user's room list, create new chatroom on first time chat */
    suspend fun getUserChatRooms(uid: String): Result<List<ChatRoom>>
    suspend fun createNewChatRoom(newRoom: ChatRoom): Result<String>
    suspend fun getEventRoom(docId: String): Result<ChatRoom>

    suspend fun sendMessage(docId: String, comment: Comment): Result<Boolean>
    suspend fun saveLastMsgRecord(docId: String, message: Comment): Result<Boolean>


    suspend fun likeOnPostComment(
        collection: String,
        docId: String,
        subCollection: String,
        subDocId: String,
        uid: String,
    ): Result<Boolean>

    suspend fun cancelLikeOnPostComment(
        collection: String,
        docId: String,
        subCollection: String,
        subDocId: String,
        uid: String,
    ): Result<Boolean>

    suspend fun saveLog(log: PostLog): Result<Boolean>
    suspend fun getUserLog(uid: String): Result<List<PostLog>>


    suspend fun updateGiftStatus(docId: String, statusCode: Int, uid: String): Result<Boolean>
    suspend fun updateEventRoom(roomId: String, user: UserInfo): Result<Boolean>
    suspend fun updateEventStatus(docId: String, code: Int): Result<Boolean>

    suspend fun updateFieldValue(
        collection: String,
        docId: String,
        field: String,
        value: FieldValue,
    ): Result<Boolean>
}
