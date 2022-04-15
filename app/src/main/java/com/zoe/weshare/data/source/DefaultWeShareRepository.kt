package com.zoe.weshare.data.source

import com.zoe.weshare.data.*

/**
 * Created by Wayne Chen on 2020-01-15.
 *
 * Concrete implementation to load Publisher sources.
 */
class DefaultWeShareRepository(
    private val remoteDataSource: WeShareDataSource,
    private val localDataSource: WeShareDataSource
) : WeShareRepository {

    override suspend fun postNewEvent(event: EventPost): Result<Boolean> {
        return remoteDataSource.postNewEvent(event)
    }

    override suspend fun postNewGift(gift: GiftPost): Result<Boolean> {
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

    override suspend fun askForGift(docId: String,comment: Comment): Result<Boolean> {
        return remoteDataSource.askForGift(docId,comment)
    }

    override suspend fun sendEventComment(docId: String, comment: Comment): Result<Boolean> {
        return remoteDataSource.sendEventComment(docId, comment)
    }

    override suspend fun getEventComments(docId: String): Result<List<Comment>> {
        return remoteDataSource.getEventComments(docId)
    }
}
