package com.zoe.weshare.data.source

import com.zoe.weshare.data.*

interface WeShareRepository {

    suspend fun postNewEvent(event: EventPost): Result<Boolean>
    suspend fun postNewGift(gift: GiftPost): Result<Boolean>
    suspend fun getGifts(): Result<List<GiftPost>>
    suspend fun getEvents(): Result<List<EventPost>>
    suspend fun getUserInfo(uid: String): Result<UserProfile>
    suspend fun getGiftAskForComments(docId: String): Result<List<Comment>>
    suspend fun askForGift(docId: String,comment: Comment): Result<Boolean>

    suspend fun sendEventComment(docId: String,comment: Comment): Result<Boolean>
    suspend fun getEventComments(docId: String): Result<List<Comment>>


}
