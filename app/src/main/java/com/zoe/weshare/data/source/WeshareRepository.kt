package com.zoe.weshare.data.source

import androidx.lifecycle.MutableLiveData
import com.zoe.weshare.data.*

interface WeShareRepository {
    suspend fun postNewEvent(event: EventPost): Result<String>
    suspend fun postNewGift(gift: GiftPost): Result<String>
    suspend fun getGifts(): Result<List<GiftPost>>
    suspend fun getEvents(): Result<List<EventPost>>
    suspend fun getUserInfo(uid: String): Result<UserProfile>

    suspend fun sendComment(collection:String, docId: String, comment: Comment, subCollection: String): Result<Boolean>
    suspend fun getAllComments(collection:String, docId: String, subCollection: String): Result<List<Comment>>

    suspend fun sendMessage(docId: String, comment: Comment): Result<Boolean>

    fun getLiveMessages(
        collection: String,
        docId: String,
        subCollection: String,
    ): MutableLiveData<List<Comment>>


    suspend fun getChatsHistory(docId: String): Result<List<MessageItem>>
    suspend fun getUserChatRooms(uid: String): Result<List<ChatRoom>>

    suspend fun likeOnPost(collection: String, docId: String, uid: String): Result<Boolean>
    suspend fun cancelLikeOnPost(collection: String, docId: String, uid: String): Result<Boolean>


    suspend fun likeOnPostComment(collection: String,
                                  docId: String,
                                  subCollection: String,
                                  subDocId: String,
                                  uid: String): Result<Boolean>

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
    suspend fun updateEventAttendee(docId: String, field: String, uid: String, ): Result<Boolean>

    suspend fun updateEventRoom(roomId: String, user: UserInfo): Result<Boolean>
    suspend fun getEventRoom(docId: String): Result<ChatRoom>
}
