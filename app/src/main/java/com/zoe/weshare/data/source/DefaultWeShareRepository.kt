package com.zoe.weshare.data.source

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

    override suspend fun getUserInfo(uid: String): Result<UserProfile> {
        return remoteDataSource.getUserInfo(uid)
    }

    override suspend fun getGiftAskForComments(docId: String): Result<List<Comment>> {
        return remoteDataSource.getGiftAskForComments(docId)
    }

    override suspend fun askForGift(docId: String, comment: Comment): Result<Boolean> {
        return remoteDataSource.askForGift(docId, comment)
    }

    override suspend fun sendEventComment(docId: String, comment: Comment): Result<Boolean> {
        return remoteDataSource.sendEventComment(docId, comment)
    }

    override suspend fun getEventComments(docId: String): Result<List<Comment>> {
        return remoteDataSource.getEventComments(docId)
    }

    override suspend fun getChatsHistory(docId: String): Result<List<MessageItem>> {
        return remoteDataSource.getChatsHistory(docId)
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

    override suspend fun likeEventPost(docId: String, uid: String): Result<Boolean> {
        return remoteDataSource.likeEventPost(docId, uid)
    }

    override suspend fun likeGiftPost(docId: String, uid: String): Result<Boolean> {
        return remoteDataSource.likeGiftPost(docId, uid)
    }

    override suspend fun cancelLikeEventPost(docId: String, uid: String): Result<Boolean> {
        return remoteDataSource.cancelLikeEventPost(docId, uid)
    }

    override suspend fun cancelLikeGiftPost(docId: String, uid: String): Result<Boolean> {
        return remoteDataSource.cancelLikeGiftPost(docId, uid)
    }

    override suspend fun likeGiftComment(
        docId: String,
        subDocId: String,
        uid: String,
    ): Result<Boolean> {
        return remoteDataSource.likeGiftComment(docId, subDocId, uid)
    }

    override suspend fun likeEventComment(
        docId: String,
        subDocId: String,
        uid: String,
    ): Result<Boolean> {
        return remoteDataSource.likeEventComment(docId, subDocId, uid)
    }

    override suspend fun cancelLikeGiftComment(
        docId: String,
        subDocId: String,
        uid: String,
    ): Result<Boolean> {
        return remoteDataSource.cancelLikeGiftComment(docId, subDocId, uid)
    }

    override suspend fun cancelLikeEventComment(
        docId: String,
        subDocId: String,
        uid: String,
    ): Result<Boolean> {
        return remoteDataSource.cancelLikeEventComment(docId, subDocId, uid)
    }

    override suspend fun saveLastMsgRecord(docId: String, message: Comment): Result<Boolean> {
        return remoteDataSource.saveLastMsgRecord(docId, message)
    }

    override suspend fun savePostLog(log: PostLog): Result<Boolean> {
        return remoteDataSource.savePostLog(log)
    }


    override suspend fun getUsersGiftLog(uid: String): Result<List<PostLog>> {
        return remoteDataSource.getUsersGiftLog(uid)
    }

    override suspend fun getUsersRequestLog(uid: String): Result<List<PostLog>> {
        return remoteDataSource.getUsersRequestLog(uid)
    }

    override suspend fun searchGiftDocument(doc: String): Result<GiftPost> {
        return remoteDataSource.searchGiftDocument(doc)
    }

    override suspend fun updateGiftStatus(docId: String, statusCode: Int): Result<Boolean> {
        return remoteDataSource.updateGiftStatus(docId, statusCode)
    }

    override suspend fun sendAwayGift(
        docId: String,
        statusCode: Int,
        uid: String,
    ): Result<Boolean> {
        return remoteDataSource.sendAwayGift(docId, statusCode, uid)
    }
}
