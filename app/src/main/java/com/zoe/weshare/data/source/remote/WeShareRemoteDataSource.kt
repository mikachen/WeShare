package com.zoe.weshare.data.source.remote

import android.icu.util.Calendar
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.data.Result
import com.zoe.weshare.data.source.WeShareDataSource
import com.zoe.weshare.util.Logger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object WeShareRemoteDataSource: WeShareDataSource {


    private const val PATH_EVENT_POST = "EventPost"
    private const val PATH_GIFT_POST = "GiftPost"

    private const val KEY_CREATED_TIME = "createdTime"

    override suspend fun postNewEvent(event: EventPost): Result<Boolean> = suspendCoroutine { continuation ->

        val eventPost = FirebaseFirestore.getInstance().collection(PATH_EVENT_POST)
        val document = eventPost.document()

        event.id = document.id
        event.createdTime = Calendar.getInstance().timeInMillis

        /** MockData */
        event.sort= "淨灘"
        event.volunteerNeeds= 4
        event.description = "測試文字測試文字測試文字測試文字測試文字測試文字測試文字"
        event.image = "https://images-cn.ssl-images-amazon.cn/images/I/71PdnmcB7gL.__AC_SY300_SX300_QL70_ML2_.jpg"

        document
            .set(event)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Logger.i("Publish: $event")

                    continuation.resume(Result.Success(true))
                } else {
                    task.exception?.let {

                        Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                }
            }
    }

    override suspend fun postNewGift(gift: GiftPost): Result<Boolean>  = suspendCoroutine { continuation ->

        val eventPost = FirebaseFirestore.getInstance().collection(PATH_GIFT_POST)
        val document = eventPost.document()

        gift.id = document.id
        gift.createdTime = Calendar.getInstance().timeInMillis

        /** MockData */
        gift.sort = "食品"
        gift.condidtion = "全新"
        gift.quantity = 8
        gift.description = "測試文字測試文字測試文字測試文字測試文字測試文字測試文字"
        gift.image =
            "https://images-cn.ssl-images-amazon.cn/images/I/71PdnmcB7gL.__AC_SY300_SX300_QL70_ML2_.jpg"

        document
            .set(gift)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Logger.i("Publish: $gift")

                    continuation.resume(Result.Success(true))
                } else {
                    task.exception?.let {

                        Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                }
            }
    }

}