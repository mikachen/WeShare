package com.zoe.weshare.detail.gift

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.ChatRoomType
import com.zoe.weshare.util.Const
import com.zoe.weshare.util.Const.PATH_GIFT_POST
import com.zoe.weshare.util.Const.SUB_PATH_GIFT_USER_WHO_ASK_FOR
import com.zoe.weshare.util.UserManager
import com.zoe.weshare.util.UserManager.userBlackList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GiftDetailViewModel(private val repository: WeShareRepository, val userInfo: UserInfo?) :
    ViewModel() {

    private var _selectedGiftDisplay = MutableLiveData<GiftPost>()
    val selectedGiftDisplay: LiveData<GiftPost>
        get() = _selectedGiftDisplay

    private var allComments = MutableLiveData<List<Comment>>()

    private var _filteredComments = MutableLiveData<List<Comment>>()
    val filteredComments: LiveData<List<Comment>>
        get() = _filteredComments

    private var _targetUser = MutableLiveData<UserInfo?>()
    val targetUser: LiveData<UserInfo?>
        get() = _targetUser

    private var _currentLikedNumber = MutableLiveData<Int>()
    val currentLikedNumber: LiveData<Int>
        get() = _currentLikedNumber

    private var _isUserPressedLike = MutableLiveData<Boolean>()
    val isUserPressedLike: LiveData<Boolean>
        get() = _isUserPressedLike

    private var _onProfileSearchComplete = MutableLiveData<Int>()
    val onProfileSearchComplete: LiveData<Int>
        get() = _onProfileSearchComplete

    private var _onCommentLikePressed = MutableLiveData<Int>()
    val onCommentLikePressed: LiveData<Int>
        get() = _onCommentLikePressed

    private var _userChatRooms = MutableLiveData<List<ChatRoom>?>()
    val userChatRooms: LiveData<List<ChatRoom>?>
        get() = _userChatRooms

    private val _navigateToFormerRoom = MutableLiveData<ChatRoom?>()
    val navigateToFormerRoom: LiveData<ChatRoom?>
        get() = _navigateToFormerRoom

    private val _navigateToNewRoom = MutableLiveData<ChatRoom?>()
    val navigateToNewRoom: LiveData<ChatRoom?>
        get() = _navigateToNewRoom

    var blockUserComplete = MutableLiveData<UserProfile>()

    val profileList = mutableListOf<UserProfile>()
    var updateCommentLike = mutableListOf<Comment>()

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    fun onGiftDisplay(gift: GiftPost) {
        _selectedGiftDisplay.value = gift
        _currentLikedNumber.value = gift.whoLiked.size
        _isUserPressedLike.value = gift.whoLiked.contains(userInfo?.uid) == true
    }

    fun getAskForGiftComments(docId: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.getAllComments(
                    collection = PATH_GIFT_POST,
                    docId = docId,
                    subCollection = SUB_PATH_GIFT_USER_WHO_ASK_FOR
                )
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    allComments.value = result.data ?: emptyList()

                    filterComment()

                    updateCommentLike = result.data as MutableList<Comment>
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _status.value = LoadApiStatus.ERROR
                    _filteredComments.value = emptyList()
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _status.value = LoadApiStatus.ERROR
                    _filteredComments.value = emptyList()
                }
                else -> {
                    _error.value =
                        WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                    _filteredComments.value = emptyList()
                }
            }
        }
    }

    fun searchUsersProfile(comments: List<Comment>?) {
        comments?.let {
            val filtered = comments.distinctBy { it.uid }
            _onProfileSearchComplete.value = filtered.size

            for (element in filtered) {
                getUserInfo(element.uid)
            }
        }
    }

    private fun getUserInfo(uid: String) {
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
            _onProfileSearchComplete.value = _onProfileSearchComplete.value?.minus(1)
        }
    }

    fun onPostLikePressed(doc: String) {

        if (isUserPressedLike.value == false) {
            sendLike(doc)
        } else {
            cancelLike(doc)
        }
    }

    private fun sendLike(doc: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.updateFieldValue(
                    collection = PATH_GIFT_POST,
                    docId = doc,
                    field = Const.FIELD_WHO_LIKED,
                    value = FieldValue.arrayUnion(userInfo!!.uid)
                )
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    _currentLikedNumber.value = _currentLikedNumber.value?.plus(1)
                    _isUserPressedLike.value = true
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
                    field = Const.FIELD_WHO_LIKED,
                    value = FieldValue.arrayRemove(userInfo!!.uid)
                )
            ) {

                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    _currentLikedNumber.value = _currentLikedNumber.value?.minus(1)
                    _isUserPressedLike.value = false
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

    private fun sendLikeOnComment(subDoc: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.likeOnPostComment(
                    collection = PATH_GIFT_POST,
                    docId = selectedGiftDisplay.value!!.id,
                    subCollection = SUB_PATH_GIFT_USER_WHO_ASK_FOR,
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
                    docId = selectedGiftDisplay.value!!.id,
                    subCollection = SUB_PATH_GIFT_USER_WHO_ASK_FOR,
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

    fun onCommentsLikePressed(comment: Comment, isUserLiked: Boolean, position: Int) {
        _onCommentLikePressed.value = position
        val whoLikedList = updateCommentLike[position].whoLiked as MutableList<String>

        if (!isUserLiked) {
            sendLikeOnComment(comment.id)
            whoLikedList.add(userInfo!!.uid)
        } else {
            cancelLikeOnComment(comment.id)
            whoLikedList.remove(userInfo!!.uid)
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
        val userUid = selectedGiftDisplay.value!!.author!!.uid

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
            participants = listOf(userInfo!!.uid, selectedGiftDisplay.value!!.author!!.uid),
            usersInfo = listOf(userInfo, selectedGiftDisplay.value!!.author!!)
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

    fun navigateToRoomComplete() {
        _userChatRooms.value = null
        _navigateToFormerRoom.value = null
        _navigateToNewRoom.value = null
    }

    fun onNavigateToTargetProfile(uid: String) {
        val target = UserInfo()
        target.uid = uid

        _targetUser.value = target
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

    fun filterComment() {
        _filteredComments.value = allComments.value?.filterNot { userBlackList.contains(it.uid) }
    }

    fun refreshCommentBoard() {
        filterComment()

        blockUserComplete.value = null
    }
}
