package com.zoe.weshare.data.source.remote

import android.icu.util.Calendar
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareDataSource
import com.zoe.weshare.ext.toDisplayFormat
import com.zoe.weshare.util.Const.FIELD_NOTIFICATION_READ
import com.zoe.weshare.util.Const.FIELD_OPERATOR_UID
import com.zoe.weshare.util.Const.FIELD_ROOM_LAST_MEG
import com.zoe.weshare.util.Const.FIELD_ROOM_LAST_SENT_TIME
import com.zoe.weshare.util.Const.FIELD_ROOM_PARTICIPANTS
import com.zoe.weshare.util.Const.FIELD_ROOM_USERS_INFO
import com.zoe.weshare.util.Const.FIELD_STATUS
import com.zoe.weshare.util.Const.FIELD_USER_IMAGE
import com.zoe.weshare.util.Const.FIELD_USER_INTRO_MSG
import com.zoe.weshare.util.Const.FIELD_USER_NAME
import com.zoe.weshare.util.Const.FIELD_WHO_GET_GIFT
import com.zoe.weshare.util.Const.FIELD_WHO_LIKED
import com.zoe.weshare.util.Const.KEY_CREATED_TIME
import com.zoe.weshare.util.Const.PATH_CHATROOM
import com.zoe.weshare.util.Const.PATH_EVENT_POST
import com.zoe.weshare.util.Const.PATH_GIFT_POST
import com.zoe.weshare.util.Const.PATH_LOG
import com.zoe.weshare.util.Const.PATH_USER
import com.zoe.weshare.util.Const.SUB_PATH_CHATROOM_MESSAGE
import com.zoe.weshare.util.Const.SUB_PATH_USER_NOTIFICATION
import com.zoe.weshare.util.Logger
import com.zoe.weshare.util.UserManager.weShareUser
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

    override suspend fun getAllGifts(): Result<List<GiftPost>> = suspendCoroutine { continuation ->
        FirebaseFirestore.getInstance()
            .collection(PATH_GIFT_POST)
            .whereNotEqualTo(FIELD_STATUS,2)
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

    override suspend fun getUserInfo(uid: String): Result<UserProfile?> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance()
                .collection(PATH_USER)
                .whereEqualTo("uid", uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var user: UserProfile? = null

                        for (document in task.result!!) {
                            Logger.d(document.id + " => " + document.data)

                            user = if (document.exists()) {
                                document.toObject(UserProfile::class.java)
                            } else {
                                null
                            }
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

    override suspend fun newUserRegister(user: UserProfile): Result<Boolean> =
        suspendCoroutine { continuation ->

            val newUser = FirebaseFirestore.getInstance().collection(PATH_USER)
            val document = newUser.document(user.uid)

            document.set(user)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("newUserRegister: $newUser")

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

    override suspend fun getAllComments(
        collection: String,
        docId: String,
        subCollection: String,
    ): Result<List<Comment>> =
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

    override fun getLiveComments(
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

                    val message = document.toObject(Comment::class.java)
                    list.add(message)
                }
                liveData.value = list
            }

        return liveData
    }


    override fun getLiveMessages(docId: String): MutableLiveData<List<MessageItem>> {

        val liveData = MutableLiveData<List<MessageItem>>()

        FirebaseFirestore.getInstance()
            .collection(PATH_CHATROOM).document(docId).collection(SUB_PATH_CHATROOM_MESSAGE)
            .orderBy(KEY_CREATED_TIME, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->

                Logger.i("addSnapshotListener detect")

                exception?.let {
                    Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                }

                val list = mutableListOf<Comment>()
                for (document in snapshot!!) {
                    Logger.d(document.id + " => " + document.data)

                    val historyMsg = document.toObject(Comment::class.java)
                    list.add(historyMsg)
                }
                // 回傳結果分類接收或發送方
                val holderItems = ChatsHistory(list).toMessageItem()

                liveData.value = holderItems
            }

        return liveData
    }

    /**
     * get event detail livedata value with SnapshotListener
     * */
    override fun getLiveEventDetail(docId: String): MutableLiveData<EventPost?> {

        val liveData = MutableLiveData<EventPost?>()

        FirebaseFirestore.getInstance()
            .collection(PATH_EVENT_POST).document(docId)
            .addSnapshotListener { snapshot, exception ->

                Logger.i("getLiveEventDetail detect")

                exception?.let {
                    Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                }

                if (snapshot != null) {
                    val event = snapshot.toObject(EventPost::class.java)

                    liveData.value = event
                }
            }

        return liveData
    }

    override fun getLiveNotifications(uid: String): MutableLiveData<List<OperationLog>> {
        val liveData = MutableLiveData<List<OperationLog>>()

        FirebaseFirestore.getInstance()
            .collection(PATH_USER).document(uid).collection(SUB_PATH_USER_NOTIFICATION)
            .orderBy(KEY_CREATED_TIME, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->

                Logger.i("getLiveNotifications detect")

                exception?.let {
                    Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                }

                val list = mutableListOf<OperationLog>()
                for (document in snapshot!!) {
                    Logger.d(document.id + " notifications=> " + document.data)

                    val log = document.toObject(OperationLog::class.java)
                    list.add(log)
                }
                liveData.value = list
            }

        return liveData
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

    override suspend fun saveLog(log: OperationLog): Result<Boolean> =
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

    override suspend fun getUserLog(uid: String): Result<List<OperationLog>> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance()
                .collection(PATH_LOG)
                .whereEqualTo(FIELD_OPERATOR_UID, uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<OperationLog>()
                        for (document in task.result!!) {
                            Logger.d(document.id + " => " + document.data)

                            val log = document.toObject(OperationLog::class.java)
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

    override fun getLiveLogs(): MutableLiveData<List<OperationLog>> {

        val liveData = MutableLiveData<List<OperationLog>>()

        FirebaseFirestore.getInstance()
            .collection(PATH_LOG)
            .orderBy(KEY_CREATED_TIME, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->

                Logger.i("addSnapshotListener detect")

                exception?.let {
                    Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                }

                val list = mutableListOf<OperationLog>()
                for (document in snapshot!!) {
                    Logger.d(document.id + " => " + document.data)

                    val log = document.toObject(OperationLog::class.java)
                    list.add(log)
                }
                liveData.value = list
            }
        return liveData
    }

    override suspend fun getUserAllGiftsPosts(
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

    override suspend fun updateFieldValue(
        collection: String,
        docId: String,
        field: String,
        value: FieldValue,
    ): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(collection)
                .document(docId)
                .update(field, value)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("updateFieldValue: $collection -> $field : $value")

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

    override suspend fun updateSubCollectionFieldValue(
        collection: String,
        docId: String,
        subCollection: String,
        subDocId: String,
        field: String,
        value: FieldValue,
    ): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(collection).document(docId)
                .collection(subCollection).document(subDocId)
                .update(field, value)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("updateSubCollectionFieldValue: $collection:$subCollection -> $field : $value")

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

    override suspend fun uploadImage(imageUri: Uri): Result<String> =
        suspendCoroutine { continuation ->

            val createdTime = Calendar.getInstance().timeInMillis
            val formatFileName = weShareUser!!.uid + "/" + createdTime.toDisplayFormat()

            val storageRef = FirebaseStorage.getInstance().reference.child("images/$formatFileName")

            storageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->

                    val result = taskSnapshot.metadata!!.reference!!.downloadUrl

                    result.addOnSuccessListener {
                        val downloadUrl = it.toString()
                        continuation.resume(Result.Success(downloadUrl))
                    }
                }

                .addOnFailureListener {
                    Logger.w(
                        "[${this::class.simpleName}] " +
                                "Error getting documents. ${it.message}"
                    )

                    continuation.resume(Result.Fail(WeShareApplication.instance.getString(R.string.result_fail)))
                }

        }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_USER)
                .document(profile.uid)
                .update(
                    mapOf(
                        FIELD_USER_NAME to profile.name,
                        FIELD_USER_INTRO_MSG to profile.introMsg,
                        FIELD_USER_IMAGE to profile.image
                    )
                ).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("updateUserProfile: $profile")

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

    override suspend fun sendNotifications(targetUid: String, log: OperationLog): Result<Boolean> =
        suspendCoroutine { continuation ->

            val notificationRef = FirebaseFirestore.getInstance().collection(PATH_USER)
                .document(targetUid).collection(SUB_PATH_USER_NOTIFICATION)

            val document = notificationRef.document()

            log.createdTime = Calendar.getInstance().timeInMillis
            log.id = document.id

            document
                .set(log)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("sendNotifications: $log")

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

    override suspend fun readNotification(
        uid: String,
        docId: String,
        read: Boolean,
    ): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_USER).document(uid)
                .collection(SUB_PATH_USER_NOTIFICATION).document(docId)
                .update(FIELD_NOTIFICATION_READ, read)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("readNotification -> $docId : $read")

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
}
