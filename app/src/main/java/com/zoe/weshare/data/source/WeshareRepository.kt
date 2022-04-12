package com.zoe.weshare.data.source

import com.zoe.weshare.data.EventPost
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.data.Result

interface WeShareRepository {

    suspend fun postNewEvent(event: EventPost): Result<Boolean>
    suspend fun postNewGift(gift: GiftPost): Result<Boolean>

    suspend fun getGifts(): Result<List<GiftPost>>
    suspend fun getEvents(): Result<List<EventPost>>
}
