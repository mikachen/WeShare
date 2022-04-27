package com.zoe.weshare.data.source.remote

import android.icu.util.Calendar
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareDataSource
import com.zoe.weshare.util.Const.FIELD_OPERATOR_UID
import com.zoe.weshare.util.Const.FIELD_ROOM_LAST_MEG
import com.zoe.weshare.util.Const.FIELD_ROOM_LAST_SENT_TIME
import com.zoe.weshare.util.Const.FIELD_ROOM_PARTICIPANTS
import com.zoe.weshare.util.Const.FIELD_ROOM_USERS_INFO
import com.zoe.weshare.util.Const.FIELD_STATUS
import com.zoe.weshare.util.Const.FIELD_WHO_GET_GIFT
import com.zoe.weshare.util.Const.FIELD_WHO_LIKED
import com.zoe.weshare.util.Const.KEY_CREATED_TIME
import com.zoe.weshare.util.Const.PATH_CHATROOM
import com.zoe.weshare.util.Const.PATH_EVENT_POST
import com.zoe.weshare.util.Const.PATH_GIFT_POST
import com.zoe.weshare.util.Const.PATH_LOG
import com.zoe.weshare.util.Const.PATH_USER
import com.zoe.weshare.util.Const.SUB_PATH_CHATROOM_MESSAGE
import com.zoe.weshare.util.Logger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object WeShareRemoteDataSource : WeShareDataSource {

    override suspend fun postNewEvent(event: EventPost): Result<String> =
        suspendCoroutine { continuation ->

            val eventPost = FirebaseFirestore.getInstance().collection(PATH_EVENT_POST)
            val document = eventPost.document()

            event.id = document.id
            event.createdTime = Calendar.getInstance().timeInMillis

            document
                .set(event)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("Publish: $event")

                        continuation.resume(Result.Success(document.id))
                    } else {
                        task.exception?.let {

                            Logger.w(
                                "[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun postNewGift(gift: GiftPost): Result<String> =
        suspendCoroutine { continuation ->

            val eventPost = FirebaseFirestore.getInstance().collection(PATH_GIFT_POST)
            val document = eventPost.document()

            gift.createdTime = Calendar.getInstance().timeInMillis
            gift.id = document.id

            document
                .set(gift)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("Publish: $gift")

                        continuation.resume(Result.Success(document.id))
                    } else {
                        task.exception?.let {

                            Logger.w(
                                "[${this::class.simpleName}]" +
                                    "Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(
                            Result.Fail(
                                WeShareApplication.instance.getString(R.string.result_fail)
                            )
                        )
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

                        Logger.w(
                            "[${this::class.simpleName}] " +
                                "Error getting documents. ${it.message}"
                        )

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

                        Logger.w(
                            "[${this::class.simpleName}] " +
                                "Error getting documents. ${it.message}"
                        )

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
                .collection(PATH_USER)
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

                            Logger.w(
                                "[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun getAllComments(collection: String, docId: String, subCollection: String): Result<List<Comment>> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance()
                .collection(collection).document(docId)
                .collection(subCollection)
                .orderBy(KEY_CREATED_TIME, Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<Comment>()
                        for (document in task.result!!) {
                            Logger.d(collection + document.id + " => " + document.data)

                            val requestComments = document.toObject(Comment::class.java)
                            list.add(requestComments)
                        }
                        continuation.resume(Result.Success(list))
                    } else {
                        task.exception?.let {

                            Logger.w(
                                "[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun sendComment(
        collection: String,
        docId: String,
        comment: Comment,
        subCollection: String,
    ): Result<Boolean> = suspendCoroutine { continuation ->

        val commentPost = FirebaseFirestore.getInstance().collection(collection)
            .document(docId).collection(subCollection)
        val document = commentPost.document()

        comment.id = document.id
        comment.createdTime = Calendar.getInstance().timeInMillis
        comment.whoLiked = listOf()

        document
            .set(comment)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Logger.i(collection + "sendComment: $comment")

                    continuation.resume(Result.Success(true))
                } else {
                    task.exception?.let {

                        Logger.w(
                            "[${this::class.simpleName}] " +
                                "Error getting documents. ${it.message}"
                        )

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

                            Logger.w(
                                "[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override fun getLiveMessages(
        collection: String,
        docId: String,
        subCollection: String,
    ): MutableLiveData<List<Comment>> {

        val liveData = MutableLiveData<List<Comment>>()

        FirebaseFirestore.getInstance()
            .collection(collection).document(docId).collection(subCollection)
            .orderBy(KEY_CREATED_TIME, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->

                Logger.i("addSnapshotListener detect")

                exception?.let {
                    Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                }

                val list = mutableListOf<Comment>()
                for (document in snapshot!!) {
                    Logger.d(document.id + " => " + document.data)

                    val article = document.toObject(Comment::class.java)
                    list.add(article)
                }
                liveData.value = list
            }

        return liveData
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

                            Logger.w(
                                "[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun getUserChatRooms(uid: String): Result<List<ChatRoom>> =
        suspendCoroutine { continuation ->

            FirebaseFirestore.getInstance()
                .collection(PATH_CHATROOM)
                .whereArrayContains(FIELD_ROOM_PARTICIPANTS, uid)
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

                            Logger.w(
                                "[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun likeOnPost(
        collection: String,
        docId: String,
        uid: String,
    ): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(collection)
                .document(docId)
                .update(FIELD_WHO_LIKED, FieldValue.arrayUnion(uid))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("LikeGiftPost: $uid")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w(
                                "[${this::class.simpleName}]" +
                                    " Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun cancelLikeOnPost(collection: String, docId: String, uid: String): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(collection)
                .document(docId)
                .update(FIELD_WHO_LIKED, FieldValue.arrayRemove(uid))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("CancelLikeGiftPost: $uid")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w(
                                "[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun likeOnPostComment(
        collection: String,
        docId: String,
        subCollection: String,
        subDocId: String,
        uid: String,
    ): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(collection).document(docId)
                .collection(subCollection).document(subDocId)
                .update(FIELD_WHO_LIKED, FieldValue.arrayUnion(uid))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("likeOnPostComment: $uid")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w(
                                "[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun cancelLikeOnPostComment(
        collection: String,
        docId: String,
        subCollection: String,
        subDocId: String,
        uid: String,
    ): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(collection).document(docId)
                .collection(subCollection).document(subDocId)
                .update(FIELD_WHO_LIKED, FieldValue.arrayRemove(uid))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("cancelLikeOnPostComment: $uid")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w(
                                "[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}"
                            )

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
                .update(
                    mapOf(
                        FIELD_ROOM_LAST_MEG to message.content,
                        FIELD_ROOM_LAST_SENT_TIME to message.createdTime
                    )
                )
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("saveLastMsgRecord: $message")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w(
                                "[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(
                            Result.Fail(
                                WeShareApplication.instance.getString(R.string.result_fail)
                            )
                        )
                    }
                }
        }

    override suspend fun createNewChatRoom(newRoom: ChatRoom): Result<String> =
        suspendCoroutine { continuation ->

            val room = FirebaseFirestore.getInstance().collection(PATH_CHATROOM)
            val document = room.document()

            newRoom.id = document.id

            document
                .set(newRoom)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("createNewChatRoom: $newRoom")

                        continuation.resume(Result.Success(document.id))
                    } else {
                        task.exception?.let {

                            Logger.w(
                                "[${this::class.simpleName}]" +
                                    "Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(
                            Result.Fail(
                                WeShareApplication.instance.getString(R.string.result_fail)
                            )
                        )
                    }
                }
        }

    override suspend fun saveLog(log: PostLog): Result<Boolean> =
        suspendCoroutine { continuation ->

            val logSave = FirebaseFirestore.getInstance().collection(PATH_LOG)
            val document = logSave.document()

            log.id = document.id
            log.createdTime = Calendar.getInstance().timeInMillis

            document
                .set(log)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("saveGiftPostLog: $log")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w(
                                "[${this::class.simpleName}]" +
                                    "Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(
                            Result.Fail(
                                WeShareApplication.instance.getString(R.string.result_fail)
                            )
                        )
                    }
                }
        }

    override suspend fun getUserLog(uid: String): Result<List<PostLog>> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance()
                .collection(PATH_LOG)
                .whereEqualTo(FIELD_OPERATOR_UID, uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<PostLog>()
                        for (document in task.result!!) {
                            Logger.d(document.id + " => " + document.data)

                            val log = document.toObject(PostLog::class.java)
                            list.add(log)
                        }

                        Logger.i("getUserLog: $list")
                        continuation.resume(Result.Success(list))
                    } else {
                        task.exception?.let {

                            Logger.w(
                                "[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(
                            Result.Fail(
                                WeShareApplication.instance.getString(R.string.result_fail)
                            )
                        )
                    }
                }
        }

    override suspend fun getUserHistoryPosts(
        collection: String,
        uid: String,
    ): Result<List<GiftPost>> =
        suspendCoroutine { continuation ->

            FirebaseFirestore.getInstance()
                .collection(collection)
                .whereEqualTo("author.uid", uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<GiftPost>()
                        for (document in task.result!!) {
                            Logger.d(document.id + " => " + document.data)

                            val log = document.toObject(GiftPost::class.java)
                            list.add(log)
                        }

                        Logger.i(collection + "getUserHistoryPosts: $list")

                        continuation.resume(Result.Success(list))
                    } else {
                        task.exception?.let {

                            Logger.w(
                                "[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(
                            Result.Fail(
                                WeShareApplication.instance.getString(R.string.result_fail)
                            )
                        )
                    }
                }
        }

    // gift -> 下架or送出贈品時
    override suspend fun updateGiftStatus(
        docId: String,
        statusCode: Int,
        uid: String,
    ): Result<Boolean> =
        suspendCoroutine { continuation ->

            FirebaseFirestore.getInstance().collection(PATH_GIFT_POST).document(docId)
                .update(
                    mapOf(
                        FIELD_STATUS to statusCode,
                        FIELD_WHO_GET_GIFT to uid
                    )
                )
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("updateGiftStatus: $statusCode")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w(
                                "[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(
                            Result.Fail(
                                WeShareApplication.instance.getString(R.string.result_fail)
                            )
                        )
                    }
                }
        }

    override suspend fun updateEventAttendee(
        docId: String,
        field: String,
        uid: String,
    ): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_EVENT_POST)
                .document(docId)
                .update(field, FieldValue.arrayUnion(uid))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("updateEventAttendee $field: $uid")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w(
                                "[${this::class.simpleName}]" +
                                    " Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun updateEventStatus(
        docId: String,
        code: Int,
    ): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_EVENT_POST)
                .document(docId)
                .update(FIELD_STATUS, code)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("updateEventStatus $code")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w(
                                "[${this::class.simpleName}]" +
                                    " Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }

    override suspend fun updateEventRoom(roomId: String, user: UserInfo): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_CHATROOM).document(roomId)
                .update(
                    mapOf(
                        FIELD_ROOM_PARTICIPANTS to FieldValue.arrayUnion(user.uid),
                        FIELD_ROOM_USERS_INFO to FieldValue.arrayUnion(user)
                    )
                )
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("updateEventRoom: $user")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w(
                                "[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(
                            Result.Fail(
                                WeShareApplication.instance.getString(R.string.result_fail)
                            )
                        )
                    }
                }
        }

    override suspend fun getEventRoom(docId: String): Result<ChatRoom> =
        suspendCoroutine { continuation ->

            FirebaseFirestore.getInstance()
                .collection(PATH_CHATROOM).document(docId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val room = task.result.toObject(ChatRoom::class.java)

                        if (room != null) {
                            Logger.d("getEventRoom" + room.id)

                            continuation.resume(Result.Success(room))
                        }
                    } else {
                        task.exception?.let {

                            Logger.w(
                                "[${this::class.simpleName}] " +
                                    "Error getting documents. ${it.message}"
                            )

                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                    }
                }
        }
}
