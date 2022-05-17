package com.zoe.weshare.detail.gift

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.detail.hasUserLikedBefore
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.ChatRoomType
import com.zoe.weshare.util.Const
import com.zoe.weshare.util.Const.FIELD_WHO_LIKED
import com.zoe.weshare.util.Const.PATH_GIFT_POST
import com.zoe.weshare.util.Const.SUB_PATH_GIFT_USER_WHO_REQUEST
import com.zoe.weshare.util.UserManager
import com.zoe.weshare.util.UserManager.userBlackList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GiftDetailViewModel(private val repository: WeShareRepository, val userInfo: UserInfo?) :
    ViewModel() {

    var liveGiftDetailResult = MutableLiveData<GiftPost?>()
    var liveRequestComments = MutableLiveData<List<Comment>>()


    private var _filteredComments = MutableLiveData<List<Comment>>()
    val filteredComments: LiveData<List<Comment>>
        get() = _filteredComments

    private var _targetUser = MutableLiveData<UserInfo?>()
    val targetUser: LiveData<UserInfo?>
        get() = _targetUser

    private var _onProfileSearchLoop = MutableLiveData<Int>()
    val onProfileSearchLoop: LiveData<Int>
        get() = _onProfileSearchLoop


    private var _userChatRooms = MutableLiveData<List<ChatRoom>?>()
    val userChatRooms: LiveData<List<ChatRoom>?>
        get() = _userChatRooms

    private val _navigateToFormerRoom = MutableLiveData<ChatRoom?>()
    val navigateToFormerRoom: LiveData<ChatRoom?>
        get() = _navigateToFormerRoom

    private val _navigateToNewRoom = MutableLiveData<ChatRoom?>()
    val navigateToNewRoom: LiveData<ChatRoom?>
        get() = _navigateToNewRoom

    private var _reportedTarget = MutableLiveData<String>()
    val reportedTarget: LiveData<String>
        get() = _reportedTarget

    var blockUserComplete = MutableLiveData<UserProfile>()

    val profileList = mutableListOf<UserProfile>()

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    fun onViewPrepare(gift: GiftPost) {
        getLiveGiftDetail(gift)
        getLiveRequestComments(gift)
    }

    private fun getLiveGiftDetail(gift: GiftPost) {
        liveGiftDetailResult = repository.getLiveGiftDetail(gift.id)

    }

    private fun getLiveRequestComments(gift: GiftPost) {
        liveRequestComments = repository.getLiveComments(
            collection = PATH_GIFT_POST,
            docId = gift.id,
            subCollection = SUB_PATH_GIFT_USER_WHO_REQUEST
        )
    }

    fun searchUsersProfile(comments: List<Comment>) {

        if (comments.isNotEmpty()) {
            val newUserUid = mutableListOf<String>()

            // first entry is always empty
            if (profileList.isEmpty()) {
                for (i in comments) {
                    newUserUid.add(i.uid)
                }

            }
            else {
                for (i in comments) {
                    if (!profileList.any { it.uid == i.uid }) {
                        newUserUid.add(i.uid)
                    }
                }
            }

            _onProfileSearchLoop.value = newUserUid.size

            Log.d("newUserUid","$newUserUid,${_onProfileSearchLoop.value}")
            for (uid in newUserUid) {
                getUserProfile(uid)
            }
        }
        else{
            //no comment case
            return
        }
    }

    private fun getUserProfile(uid: String) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (val result = repository.getUserInfo(uid)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    profileList.add(result.data!!)
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _status.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _status.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value =
                        WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
            _onProfileSearchLoop.value = _onProfileSearchLoop.value?.minus(1)
        }
    }

    fun onPostLikePressed(gift: GiftPost) {
        if (!hasUserLikedBefore(gift.whoLiked)) {
            sendLike(gift.id)
        } else {
            cancelLike(gift.id)
        }
    }

    private fun sendLike(doc: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.updateFieldValue(
                    collection = PATH_GIFT_POST,
                    docId = doc,
                    field = FIELD_WHO_LIKED,
                    value = FieldValue.arrayUnion(userInfo!!.uid)
                )
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                }
                is Result.Fail -> {
                    _error.value = result.error
                    _status.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _status.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    private fun cancelLike(doc: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.updateFieldValue(
                    collection = PATH_GIFT_POST,
                    docId = doc,
                    field = FIELD_WHO_LIKED,
                    value = FieldValue.arrayRemove(userInfo!!.uid)
                )
            ) {

                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                }
                is Result.Fail -> {
                    _error.value = result.error
                    _status.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _status.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun onCommentsLikePressed(comment: Comment) {

        if (!hasUserLikedBefore(comment.whoLiked)) {
            sendLikeOnComment(comment.id)
        } else {
            cancelLikeOnComment(comment.id)
        }
    }

    private fun sendLikeOnComment(subDoc: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.likeOnPostComment(
                    collection = PATH_GIFT_POST,
                    docId = liveGiftDetailResult.value!!.id,
                    subCollection = SUB_PATH_GIFT_USER_WHO_REQUEST,
                    subDocId = subDoc,
                    uid = userInfo!!.uid
                )
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _status.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _status.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    private fun cancelLikeOnComment(subDoc: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.cancelLikeOnPostComment(
                    collection = PATH_GIFT_POST,
                    docId = liveGiftDetailResult.value!!.id,
                    subCollection = SUB_PATH_GIFT_USER_WHO_REQUEST,
                    subDocId = subDoc,
                    uid = userInfo!!.uid
                )
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _status.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _status.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun searchOnPrivateRoom(user: UserInfo) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            val result = repository.getUserChatRooms(user.uid)

            _userChatRooms.value = when (result) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE
                    result.data
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _status.value = LoadApiStatus.ERROR
                    null
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _status.value = LoadApiStatus.ERROR
                    null
                }
                else -> {
                    _error.value =
                        WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                    null
                }
            }
        }
    }

    fun checkIfPrivateRoomExist(rooms: List<ChatRoom>) {
        val userUid = liveGiftDetailResult.value!!.author!!.uid

        val result = rooms.filter {
            it.participants.contains(userUid) && it.type == ChatRoomType.PRIVATE.value
        }

        if (result.isNotEmpty()) {
            // there was chat room history with author & ChatRoomType is PRIVATE
            _navigateToFormerRoom.value = result.single()
        } else {
            // no private chat with author before
            onNewRoomPrepare()
        }
    }

    fun onNewRoomPrepare() {
        val room = ChatRoom(
            type = ChatRoomType.PRIVATE.value,
            participants = listOf(userInfo!!.uid, liveGiftDetailResult.value!!.author!!.uid),
            usersInfo = listOf(userInfo, liveGiftDetailResult.value!!.author!!)
        )
        createRoom(room)
    }

    private fun createRoom(room: ChatRoom) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.createNewChatRoom(room)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE
                    _navigateToNewRoom.value = room.apply {
                        id = result.data
                    }
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _status.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _status.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value =
                        WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun onNavigateToTargetProfile(uid: String) {
        val target = UserInfo()
        target.uid = uid

        _targetUser.value = target
    }

    fun navigateToRoomComplete() {
        _userChatRooms.value = null
        _navigateToFormerRoom.value = null
        _navigateToNewRoom.value = null
    }

    fun navigateToProfileComplete() {
        _targetUser.value = null
    }

    fun blockThisUser(target: UserProfile) {
        _status.value = LoadApiStatus.LOADING

        coroutineScope.launch {
            when (
                val result = repository.updateFieldValue(
                    collection = Const.PATH_USER,
                    docId = UserManager.weShareUser!!.uid,
                    field = Const.FIELD_USER_BLACKLIST,
                    value = FieldValue.arrayUnion(target.uid)
                )
            ) {
                is Result.Success -> {
                    userBlackList.add(target.uid)

                    blockUserComplete.value = target
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _status.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _status.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun filterRequestComments() {
        _filteredComments.value =
            liveRequestComments.value?.filterNot { userBlackList.contains(it.uid) }
    }

    fun refreshCommentBoard() {

        filterRequestComments()
        blockUserComplete.value = null
    }

    fun onNavigateToReportDialog(target: UserProfile) {

        _reportedTarget.value = target.uid
    }

    fun navigateToReportComplete(){
        _reportedTarget.value = null
    }
}
