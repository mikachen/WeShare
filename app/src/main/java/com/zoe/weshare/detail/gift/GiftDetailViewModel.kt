package com.zoe.weshare.detail.gift

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.detail.isUserLikedBefore
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.ChatRoomType
import com.zoe.weshare.util.Const.FIELD_USER_BLACKLIST
import com.zoe.weshare.util.Const.FIELD_WHO_LIKED
import com.zoe.weshare.util.Const.PATH_GIFT_POST
import com.zoe.weshare.util.Const.PATH_USER
import com.zoe.weshare.util.Const.SUB_PATH_GIFT_USER_WHO_REQUEST
import com.zoe.weshare.util.UserManager.userBlackList
import com.zoe.weshare.util.UserManager.weShareUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GiftDetailViewModel(private val repository: WeShareRepository, val userInfo: UserInfo) :
    ViewModel() {

    private lateinit var gift: GiftPost

    var liveGiftDetailResult = MutableLiveData<GiftPost?>()
    var liveRequestComments = MutableLiveData<List<Comment>>()

    private var _filteredComments = MutableLiveData<List<Comment>>()
    val filteredComments: LiveData<List<Comment>>
        get() = _filteredComments

    private var _onTargetAvatarClicked = MutableLiveData<UserInfo?>()
    val onTargetAvatarClicked: LiveData<UserInfo?>
        get() = _onTargetAvatarClicked

    val profileList = mutableListOf<UserProfile>()
    private var onSearchUserProfile = MutableLiveData<Int>()

    private var _profileSearchComplete = MutableLiveData<Boolean>()
    val profileSearchComplete: LiveData<Boolean>
        get() = _profileSearchComplete

    private var _userChatRoomsResult = MutableLiveData<List<ChatRoom>?>()
    val userChatRoomsResult: LiveData<List<ChatRoom>?>
        get() = _userChatRoomsResult

    private val _navigateToFormerRoom = MutableLiveData<ChatRoom?>()
    val navigateToFormerRoom: LiveData<ChatRoom?>
        get() = _navigateToFormerRoom

    private val _navigateToNewRoom = MutableLiveData<ChatRoom?>()
    val navigateToNewRoom: LiveData<ChatRoom?>
        get() = _navigateToNewRoom

    private val _navigateToRequest = MutableLiveData<GiftPost?>()
    val navigateToRequest: LiveData<GiftPost?>
        get() = _navigateToRequest

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    var onReportUserViolation = MutableLiveData<String?>()
    var onBlockListUser = MutableLiveData<UserProfile?>()

    init {
        _profileSearchComplete.value = false
    }

    fun onViewPrepare(selectedGift: GiftPost) {
        getLiveGiftDetail(selectedGift)
        getLiveRequestComments(selectedGift)
    }

    fun setGift(gift: GiftPost){
        this.gift = gift
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


    /**
     * To get user's avatar and name,
     * this will decide if the user profile has been queried before,
     * and only sort out the new user profile that haven't been query
     * */
    fun searchUsersProfile(comments: List<Comment>) {

        if (comments.isNotEmpty()) {
            _profileSearchComplete.value = false

            val newUserUid = mutableListOf<String>()

            // first entry is always empty
            if (profileList.isEmpty()) {
                for (comment in comments) {
                    newUserUid.add(comment.uid)
                }

            } else {
                for (comment in comments) {
                    if (alreadyGotUserProfile(comment)) {
                        newUserUid.add(comment.uid)
                    }
                }
            }

            if (newUserUid.size == 0) {
                _profileSearchComplete.value = true

            } else {
                onSearchUserProfile.value = newUserUid.size

                for (uid in newUserUid) {
                    getUserProfile(uid)
                }
            }
        }
    }

    private fun alreadyGotUserProfile(comment: Comment): Boolean {
        return !profileList.any { it.uid == comment.uid }
    }

    private fun getUserProfile(uid: String) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (val result = repository.getUserInfo(uid)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    val profile = result.data

                    if (profile != null) {
                        profileList.add(profile)
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
            onSearchUserProfile.value = onSearchUserProfile.value?.minus(1)

            if (onSearchUserProfile.value == 0) {
                _profileSearchComplete.value = true
            }
        }
    }

    fun isGetProfileDone(): Boolean {
        return profileSearchComplete.value == true && profileList.isNotEmpty()
    }

    fun onPostLikePressed(gift: GiftPost) {
        if (!isUserLikedBefore(gift.whoLiked)) {
            sendLike(gift.id)
        } else {
            cancelLike(gift.id)
        }
    }

    private fun sendLike(doc: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.updateFieldValue(
                    collection = PATH_GIFT_POST,
                    docId = doc,
                    field = FIELD_WHO_LIKED,
                    value = FieldValue.arrayUnion(userInfo.uid))
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

            when (val result = repository.updateFieldValue(
                    collection = PATH_GIFT_POST,
                    docId = doc,
                    field = FIELD_WHO_LIKED,
                    value = FieldValue.arrayRemove(userInfo.uid))
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

        if (!isUserLikedBefore(comment.whoLiked)) {
            sendLikeOnComment(comment.id)
        } else {
            cancelLikeOnComment(comment.id)
        }
    }

    private fun sendLikeOnComment(subDoc: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.updateSubCollectionFieldValue(
                    collection = PATH_GIFT_POST,
                    docId = gift.id,
                    subCollection = SUB_PATH_GIFT_USER_WHO_REQUEST,
                    subDocId = subDoc,
                    field = FIELD_WHO_LIKED,
                    value = FieldValue.arrayUnion(userInfo.uid))
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

            when (val result = repository.updateSubCollectionFieldValue(
                    collection = PATH_GIFT_POST,
                    docId = gift.id,
                    subCollection = SUB_PATH_GIFT_USER_WHO_REQUEST,
                    subDocId = subDoc,
                    field = FIELD_WHO_LIKED,
                    value = FieldValue.arrayRemove(userInfo.uid))
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

            _userChatRoomsResult.value = when (result) {
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

        if (hasFormerRoomWithAuthor(rooms)) {
            _navigateToFormerRoom.value = getFormerRoom(rooms)
        } else {

            onNewRoomPrepare()
        }
    }

    private fun hasFormerRoomWithAuthor(rooms: List<ChatRoom>): Boolean {
        // return true if user already have a PRIVATE chatroom with author
        return rooms.any {
            it.participants.contains(gift.author.uid) && it.type == ChatRoomType.PRIVATE.value
        }
    }

    private fun getFormerRoom(rooms: List<ChatRoom>): ChatRoom {
        return rooms.single {
            it.participants.contains(gift.author.uid) && it.type == ChatRoomType.PRIVATE.value }
    }

    fun onNewRoomPrepare() {
        val room = ChatRoom(
            type = ChatRoomType.PRIVATE.value,
            participants = listOf(userInfo.uid, gift.author.uid),
            usersInfo = listOf(userInfo, gift.author)
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

                    val roomId = result.data
                    room.id = roomId

                    _navigateToNewRoom.value = room
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

    fun getSpeakerProfile(comment: Comment): UserProfile? {
        return profileList.singleOrNull { it.uid == comment.uid }
    }

    fun blockUser(target: UserProfile) {
        _status.value = LoadApiStatus.LOADING

        coroutineScope.launch {
            when (val result = repository.updateFieldValue(
                    collection = PATH_USER,
                    docId = weShareUser.uid,
                    field = FIELD_USER_BLACKLIST,
                    value = FieldValue.arrayUnion(target.uid))
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    userBlackList.add(target.uid)
                    onBlockListUser.value = target
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
        onBlockListUser.value = null
    }


    fun onNavigateToTargetProfile(uid: String) {
        val target = UserInfo()
        target.uid = uid

        _onTargetAvatarClicked.value = target
    }

    fun onNavigateToRequestDialog() {
        _navigateToRequest.value = gift
    }

    fun onNavigateToRequestComplete() {
        _navigateToRequest.value = null
    }

    fun navigateToRoomComplete() {
        _userChatRoomsResult.value = null
        _navigateToFormerRoom.value = null
        _navigateToNewRoom.value = null
    }

    fun navigateToProfileComplete() {
        _onTargetAvatarClicked.value = null
    }

    fun onNavigateToReportDialog(target: UserProfile) {
        onReportUserViolation.value = target.uid
    }

    fun navigateToReportComplete() {
        onReportUserViolation.value = null
    }
}
