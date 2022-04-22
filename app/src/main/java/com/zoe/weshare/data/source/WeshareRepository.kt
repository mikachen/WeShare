package com.zoe.weshare.data.source

import com.zoe.weshare.data.*

interface WeShareRepository {
    suspend fun postNewEvent(event: EventPost): Result<String>
    suspend fun postNewGift(gift: GiftPost): Result<String>
    suspend fun getGifts(): Result<List<GiftPost>>
    suspend fun getEvents(): Result<List<EventPost>>
    suspend fun getUserInfo(uid: String): Result<UserProfile>
    suspend fun getGiftAskForComments(docId: String): Result<List<Comment>>
    suspend fun askForGift(docId: String, comment: Comment): Result<Boolean>
    suspend fun sendEventComment(docId: String, comment: Comment): Result<Boolean>
    suspend fun getEventComments(docId: String): Result<List<Comment>>
    suspend fun sendMessage(docId: String, comment: Comment): Result<Boolean>
    suspend fun getChatsHistory(docId: String): Result<List<MessageItem>>
    suspend fun getUserChatRooms(uid: String): Result<List<ChatRoom>>
    suspend fun likeEventPost(docId: String, uid: String): Result<Boolean>
    suspend fun likeGiftPost(docId: String, uid: String): Result<Boolean>
    suspend fun cancelLikeEventPost(docId: String, uid: String): Result<Boolean>
    suspend fun cancelLikeGiftPost(docId: String, uid: String): Result<Boolean>
    suspend fun likeGiftComment(docId: String, subDocId: String, uid: String): Result<Boolean>
    suspend fun likeEventComment(docId: String, subDocId: String, uid: String): Result<Boolean>
    suspend fun cancelLikeGiftComment(docId: String, subDocId: String, uid: String): Result<Boolean>
    suspend fun cancelLikeEventComment(docId: String, subDocId: String, uid: String): Result<Boolean>
    suspend fun saveLastMsgRecord(docId: String, message: Comment): Result<Boolean>
    suspend fun createNewChatRoom(newRoom: ChatRoom): Result<String>
    suspend fun saveGiftPostLog(log: PostLog, uid: String): Result<Boolean>
    suspend fun saveGiftRequestLog(log: PostLog, uid: String): Result<Boolean>

}
