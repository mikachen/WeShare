package com.zoe.weshare.data.source.local

import android.content.Context
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.data.Result
import com.zoe.weshare.data.source.WeShareDataSource


/*
 * Concrete implementation of a WeShare source as a db.
 */
class WeShareLocalDataSource(val context: Context) : WeShareDataSource {

    override suspend fun postNewEvent(event: EventPost): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun postNewGift(gift: GiftPost): Result<Boolean> {
        TODO("Not yet implemented")
    }

}