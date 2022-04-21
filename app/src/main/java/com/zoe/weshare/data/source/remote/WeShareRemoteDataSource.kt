package com.zoe.weshare.data.source.remote

import android.icu.util.Calendar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareDataSource
import com.zoe.weshare.util.Const.FIELD_ROOM_LAST_MEG
import com.zoe.weshare.util.Const.FIELD_ROOM_LAST_SENT_TIME
import com.zoe.weshare.util.Const.FIELD_WHO_LIKED
import com.zoe.weshare.util.Const.KEY_CREATED_TIME
import com.zoe.weshare.util.Const.PATH_CHATROOM
import com.zoe.weshare.util.Const.PATH_EVENT_POST
import com.zoe.weshare.util.Const.PATH_GIFT_POST
import com.zoe.weshare.util.Const.PATH_USER_INFO
import com.zoe.weshare.util.Const.SUB_PATH_CHATROOM_MESSAGE
import com.zoe.weshare.util.Const.SUB_PATH_EVENT_USER_WHO_COMMENT
import com.zoe.weshare.util.Const.SUB_PATH_GIFT_USER_WHO_ASK_FOR
import com.zoe.weshare.util.Logger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object WeShareRemoteDataSource : WeShareDataSource {

    override suspend fun postNewEvent(event: EventPost): Result<Boolean> =
        suspendCoroutine { continuation ->

            val eventPost = FirebaseFirestore.getInstance().collection(PATH_EVENT_POST)
            val document = eventPost.document()

            event.id = document.id
            event.createdTime = Calendar.getInstance().timeInMillis
            event.whoLiked = listOf()

            event.image =
                "https://img.ltn.com.tw/Upload/news/600/2019/09/29/2930967_2_1.jpg"
            document
                .set(event)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("Publish: $event")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun postNewGift(gift: GiftPost): Result<Boolean> =
        suspendCoroutine { continuation ->

            val eventPost = FirebaseFirestore.getInstance().collection(PATH_GIFT_POST)
            val document = eventPost.document()

            gift.id = document.id
            gift.createdTime = Calendar.getInstance().timeInMillis
            gift.whoLiked = listOf()
            gift.image =
                "https://cs-a.ecimg.tw/items/DBAB08A39148142/000001_1553076513.jpg"

            document
                .set(gift)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("Publish: $gift")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}]" +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun getGifts(): Result<List<GiftPost>> = suspendCoroutine { continuation ->
        FirebaseFirestore.getInstance()
            .collection(PATH_GIFT_POST).orderBy(KEY_CREATED_TIME, Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val list = mutableListOf<GiftPost>()
                    for (document in task.result!!) {
                        Logger.d(document.id + " => " + document.data)

                        val gifts = document.toObject(GiftPost::class.java)
                        list.add(gifts)
                    }
                    continuation.resume(Result.Success(list))
                } else {
                    task.exception?.let {

                        Logger.w("[${this::class.simpleName}] " +
                                "Error getting documents. ${it.message}")

                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                }
            }
    }

    override suspend fun getEvents(): Result<List<EventPost>> = suspendCoroutine { continuation ->
        FirebaseFirestore.getInstance()
            .collection(PATH_EVENT_POST).orderBy(KEY_CREATED_TIME, Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val list = mutableListOf<EventPost>()
                    for (document in task.result!!) {
                        Logger.d(document.id + " => " + document.data)

                        val events = document.toObject(EventPost::class.java)
                        list.add(events)
                    }
                    continuation.resume(Result.Success(list))
                } else {
                    task.exception?.let {

                        Logger.w("[${this::class.simpleName}] " +
                                "Error getting documents. ${it.message}")

                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                }
            }
    }

    override suspend fun getUserInfo(uid: String): Result<UserProfile> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance()
                .collection(PATH_USER_INFO)
                .whereEqualTo("uid", uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var user = UserProfile()

                        for (document in task.result!!) {
                            Logger.d(document.id + " => " + document.data)

                            user = document.toObject(UserProfile::class.java)
                        }

                        continuation.resume(Result.Success(user))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun getGiftAskForComments(docId: String): Result<List<Comment>> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance()
                .collection(PATH_GIFT_POST).document(docId)
                .collection(SUB_PATH_GIFT_USER_WHO_ASK_FOR)
                .orderBy(KEY_CREATED_TIME, Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<Comment>()
                        for (document in task.result!!) {
                            Logger.d(document.id + " => " + document.data)

                            val requestComments = document.toObject(Comment::class.java)
                            list.add(requestComments)
                        }
                        continuation.resume(Result.Success(list))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun askForGift(docId: String, comment: Comment): Result<Boolean> =
        suspendCoroutine { continuation ->

            val commentPost = FirebaseFirestore.getInstance().collection(PATH_GIFT_POST)
                .document(docId).collection(SUB_PATH_GIFT_USER_WHO_ASK_FOR)
            val document = commentPost.document()

            comment.id = document.id
            comment.createdTime = Calendar.getInstance().timeInMillis
            comment.whoLiked = listOf()

            document
                .set(comment)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("askForGiftComment: $comment")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun sendEventComment(docId: String, comment: Comment): Result<Boolean> =
        suspendCoroutine { continuation ->

            val commentPost = FirebaseFirestore.getInstance().collection(PATH_EVENT_POST)
                .document(docId).collection(SUB_PATH_EVENT_USER_WHO_COMMENT)
            val document = commentPost.document()

            comment.id = document.id
            comment.createdTime = Calendar.getInstance().timeInMillis
            comment.whoLiked = listOf()

            document
                .set(comment)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("sendEventComment: $comment")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun getEventComments(docId: String): Result<List<Comment>> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance()
                .collection(PATH_EVENT_POST).document(docId)
                .collection(SUB_PATH_EVENT_USER_WHO_COMMENT)
                .orderBy(KEY_CREATED_TIME, Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<Comment>()
                        for (document in task.result!!) {
                            Logger.d(document.id + " => " + document.data)

                            val requestComments = document.toObject(Comment::class.java)
                            list.add(requestComments)
                        }
                        continuation.resume(Result.Success(list))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun sendMessage(docId: String, comment: Comment): Result<Boolean> =
        suspendCoroutine { continuation ->
            val newMessage = FirebaseFirestore.getInstance().collection(PATH_CHATROOM)
                .document(docId).collection(SUB_PATH_CHATROOM_MESSAGE)
            val document = newMessage.document()

            comment.id = document.id

            document
                .set(comment)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("sendNewMessage: $comment")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun getChatsHistory(docId: String): Result<List<MessageItem>> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance()
                .collection(PATH_CHATROOM).document(docId)
                .collection(SUB_PATH_CHATROOM_MESSAGE)
                .orderBy(KEY_CREATED_TIME, Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<Comment>()

                        for (document in task.result!!) {
                            Logger.d(document.id + " => " + document.data)

                            val historyMessage = document.toObject(Comment::class.java)
                            list.add(historyMessage)
                        }

                        // 回傳結果分類接收或發送方
                        val chatsHistory = ChatsHistory(list)
                        continuation.resume(Result.Success(chatsHistory.toMessageItem()))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun getRelatedChatRooms(uid: String): Result<List<ChatRoom>> =
        suspendCoroutine { continuation ->

            FirebaseFirestore.getInstance()
                .collection(PATH_CHATROOM)
                .whereArrayContains("participants", uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<ChatRoom>()

                        for (document in task.result!!) {
                            Logger.d(document.id + " => " + document.data)

                            val result = document.toObject(ChatRoom::class.java)

                            list.add(result)
                        }

                        continuation.resume(Result.Success(list))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun likeGiftPost(docId: String, uid: String): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_GIFT_POST)
                .document(docId)
                .update(FIELD_WHO_LIKED, FieldValue.arrayUnion(uid))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("LikeGiftPost: $uid")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}]" +
                                    " Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun cancelLikeGiftPost(docId: String, uid: String): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_GIFT_POST)
                .document(docId)
                .update(FIELD_WHO_LIKED, FieldValue.arrayRemove(uid))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("CancelLikeGiftPost: $uid")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun likeEventPost(docId: String, uid: String): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_EVENT_POST)
                .document(docId)
                .update(FIELD_WHO_LIKED, FieldValue.arrayUnion(uid))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("LikeEventPost: $uid")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun cancelLikeEventPost(docId: String, uid: String): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_EVENT_POST)
                .document(docId)
                .update(FIELD_WHO_LIKED, FieldValue.arrayRemove(uid))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("CancelLikeEventPost: $uid")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun likeGiftComment(
        docId: String,
        subDocId: String,
        uid: String,
    ): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_GIFT_POST).document(docId)
                .collection(SUB_PATH_GIFT_USER_WHO_ASK_FOR).document(subDocId)
                .update(FIELD_WHO_LIKED, FieldValue.arrayUnion(uid))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("LikeGiftComment: $uid")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun cancelLikeGiftComment(
        docId: String,
        subDocId: String,
        uid: String,
    ): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_GIFT_POST).document(docId)
                .collection(SUB_PATH_GIFT_USER_WHO_ASK_FOR).document(subDocId)
                .update(FIELD_WHO_LIKED, FieldValue.arrayRemove(uid))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("CancelLikeGiftComment: $uid")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun likeEventComment(
        docId: String,
        subDocId: String,
        uid: String,
    ): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_EVENT_POST).document(docId)
                .collection(SUB_PATH_EVENT_USER_WHO_COMMENT).document(subDocId)
                .update(FIELD_WHO_LIKED, FieldValue.arrayUnion(uid))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("LikeEventComment: $uid")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun cancelLikeEventComment(
        docId: String,
        subDocId: String,
        uid: String,
    ): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_EVENT_POST).document(docId)
                .collection(SUB_PATH_EVENT_USER_WHO_COMMENT).document(subDocId)
                .update(FIELD_WHO_LIKED, FieldValue.arrayRemove(uid))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("CancelLikeEventComment: $uid")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun saveLastMsgRecord(docId: String, message: Comment): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_CHATROOM).document(docId)
                .update(mapOf(
                    FIELD_ROOM_LAST_MEG to message.content,
                    FIELD_ROOM_LAST_SENT_TIME to message.createdTime
                ))

                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("saveLastMsgRecord: $message")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}")

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }
}
