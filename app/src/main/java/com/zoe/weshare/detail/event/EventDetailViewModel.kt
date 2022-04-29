package com.zoe.weshare.detail.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.Const.FIELD_WHO_LIKED
import com.zoe.weshare.util.Const.PATH_EVENT_POST
import com.zoe.weshare.util.Const.SUB_PATH_EVENT_USER_WHO_COMMENT
import com.zoe.weshare.util.EventStatusType
import com.zoe.weshare.util.LogType
import com.zoe.weshare.util.Util.getString
import com.zoe.weshare.util.Util.getStringWithStrParm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class EventDetailViewModel(private val repository: WeShareRepository, val userInfo: UserInfo?) :
    ViewModel() {

    var liveComments = MutableLiveData<List<Comment>>()

    var onEventDisplaying = MutableLiveData<EventPost?>()

    private var _newComment = MutableLiveData<Comment>()
    val newComment: LiveData<Comment>
        get() = _newComment

    private var _room = MutableLiveData<ChatRoom?>()
    val room: LiveData<ChatRoom?>
        get() = _room

    private var _targetUser = MutableLiveData<UserInfo>()
    val targetUser: LiveData<UserInfo>
        get() = _targetUser

    private var _eventStatusChanged = MutableLiveData<Int>()
    val eventStatusChanged: LiveData<Int>
        get() = _eventStatusChanged

    private var _onNavigateToRoom = MutableLiveData<ChatRoom?>()
    val onNavigateToRoom: LiveData<ChatRoom?>
        get() = _onNavigateToRoom

    private val _refreshStatus = MutableLiveData<Boolean>()
    val refreshStatus: LiveData<Boolean>
        get() = _refreshStatus

    private var _currentLikedNumber = MutableLiveData<Int>()
    val currentLikedNumber: LiveData<Int>
        get() = _currentLikedNumber

    private var _isUserPressedLike = MutableLiveData<Boolean>()
    val isUserPressedLike: LiveData<Boolean>
        get() = _isUserPressedLike

    private var _onProfileSearch = MutableLiveData<Int>()
    val onProfileSearch: LiveData<Int>
        get() = _onProfileSearch

    val profileList = mutableListOf<UserProfile>()

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    private val _saveLogComplete = MutableLiveData<LoadApiStatus>()
    val saveLogComplete: LiveData<LoadApiStatus>
        get() = _saveLogComplete

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _updateRoomStatus = MutableLiveData<LoadApiStatus>()
    val updateRoomStatus: LiveData<LoadApiStatus>
        get() = _updateRoomStatus

    private val _userAttendType = MutableLiveData<String>()
    val userAttendType: LiveData<String>
        get() = _userAttendType

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error


    fun searchUsersProfile(comments: List<Comment>) {
        if (comments.isNotEmpty()) {

            val newUserUid = mutableListOf<String>()
            val filteredUser = comments.distinctBy { it.uid }

            if (profileList.isNotEmpty()) {
                for (i in filteredUser) {
                    if (!profileList.any { it.uid == i.uid }) {
                        newUserUid.add(i.uid)
                    }
                }
            }
            // first time is empty
            else {
                for (i in filteredUser) {
                    newUserUid.add(i.uid)
                }
            }

            _onProfileSearch.value = newUserUid.size

            for (uid in newUserUid) {
                getUserInfo(uid)
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
                        getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
            _onProfileSearch.value = _onProfileSearch.value?.minus(1)
        }
    }

    private fun sendLike(doc: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.updateFieldValue(
                    collection = PATH_EVENT_POST,
                    docId = doc,
                    field = FIELD_WHO_LIKED,
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
                    _error.value = getString(R.string.result_fail)
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
                    collection = PATH_EVENT_POST,
                    docId = doc,
                    field = FIELD_WHO_LIKED,
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
                    _error.value = getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun onViewPrepare(event: EventPost) {
        _currentLikedNumber.value = event.whoLiked.size
        _isUserPressedLike.value = event.whoLiked.contains(userInfo!!.uid) == true

        checkEventStatus(event)
        getLiveEventDetail(event)
        getLiveCommentResult(event)
    }

    fun onPostLikePressed(doc: String) {
        if (_isUserPressedLike.value == false) {
            sendLike(doc)
        } else {
            cancelLike(doc)
        }
    }

    fun onSendNewComment(message: String) {
        _newComment.value = Comment(
            uid = userInfo!!.uid,
            content = message
        )
    }

    fun sendComment(docId: String, comment: Comment) {
        coroutineScope.launch {

            when (
                val result = repository.sendComment(
                    collection = PATH_EVENT_POST,
                    docId = docId,
                    comment = comment,
                    subCollection = SUB_PATH_EVENT_USER_WHO_COMMENT
                )
            ) {
                is Result.Success -> {
                    _error.value = null

                    onSaveOperateLog(
                        logType = LogType.COMMENT_EVENT.value,
                        logMsg = WeShareApplication.instance.getString(
                            R.string.log_msg_send_event_comment,
                            userInfo!!.name,
                            onEventDisplaying.value!!.title
                        )
                    )
                }
                is Result.Fail -> {
                    _error.value = result.error
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                }
                else -> {
                    _error.value = getString(R.string.result_fail)
                }
            }
        }
    }

    fun onSaveOperateLog(logType: Int, logMsg: String) {
        val log = PostLog(
            logType = logType,
            logMsg = logMsg,
            postDocId = onEventDisplaying.value!!.id,
            operatorUid = userInfo!!.uid
        )
        saveLog(log)
    }

    private fun saveLog(log: PostLog) {
        coroutineScope.launch {

            _saveLogComplete.value = LoadApiStatus.LOADING

            when (val result = repository.saveLog(log)) {
                is Result.Success -> {
                    _error.value = null
                    _saveLogComplete.value = LoadApiStatus.DONE
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _saveLogComplete.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _saveLogComplete.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value = getString(R.string.result_fail)
                    _saveLogComplete.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun onCommentsLikePressed(comment: Comment, isUserLiked: Boolean) {
        if (!isUserLiked) {
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
                    collection = PATH_EVENT_POST,
                    docId = onEventDisplaying.value!!.id,
                    subCollection = SUB_PATH_EVENT_USER_WHO_COMMENT,
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
                    _error.value = getString(R.string.result_fail)
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
                    collection = PATH_EVENT_POST,
                    docId = onEventDisplaying.value!!.id,
                    subCollection = SUB_PATH_EVENT_USER_WHO_COMMENT,
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
                    _error.value = getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun onAttendEvent(fieldString: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.updateFieldValue(
                collection = PATH_EVENT_POST,
                docId = onEventDisplaying.value!!.id,
                field = fieldString,
                value = FieldValue.arrayUnion(userInfo!!.uid)
            )) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    _userAttendType.value = fieldString
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
                    _error.value = getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun getLiveEventDetail(event: EventPost) {

        onEventDisplaying = repository.getLiveEventDetail(docId = event.id)

        _status.value = LoadApiStatus.DONE
        _refreshStatus.value = false
    }


    fun getLiveCommentResult(event: EventPost) {
        liveComments = repository.getLiveComments(
            collection = PATH_EVENT_POST,
            docId = event.id,
            subCollection = SUB_PATH_EVENT_USER_WHO_COMMENT
        )

        _status.value = LoadApiStatus.DONE
        _refreshStatus.value = false
    }

    fun getChatRoomInfo() {
        coroutineScope.launch {
            when (val result = repository.getEventRoom(docId = onEventDisplaying.value!!.roomId)) {
                is Result.Success -> {
                    _error.value = null
                    _room.value = result.data
                }
                is Result.Fail -> {
                    _error.value = result.error
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                }
                else -> {
                    _error.value = getString(R.string.result_fail)
                }
            }
        }
    }

    fun updateRoomParticipants() {
        coroutineScope.launch {
            _updateRoomStatus.value = LoadApiStatus.LOADING

            when (
                val result = repository.updateEventRoom(
                    roomId = onEventDisplaying.value!!.roomId,
                    user = userInfo!!
                )
            ) {
                is Result.Success -> {
                    _error.value = null
                    _updateRoomStatus.value = LoadApiStatus.DONE
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _updateRoomStatus.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _updateRoomStatus.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value = getString(R.string.result_fail)
                    _updateRoomStatus.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun checkUserInRoomBefore(room: ChatRoom) {

        if (room.participants.isNotEmpty()) {
            if (room.participants.contains(userInfo!!.uid)) {

                _onNavigateToRoom.value = room

            } else {
                // a new user never been in room before
                updateRoomParticipants()
            }
        } else {
            // the first user want to enter this room
            updateRoomParticipants()
        }
    }

    fun navigateToRoomComplete() {
        _onNavigateToRoom.value = null
        _room.value = null
    }

    fun checkEventStatus(event: EventPost) {
        when (true) {
            (event.startTime > Calendar.getInstance().timeInMillis) -> {
                if (event.status != EventStatusType.WAITING.code) {
                    _eventStatusChanged.value = EventStatusType.WAITING.code
                }
            }
            (event.startTime < Calendar.getInstance().timeInMillis) -> {
                if (event.endTime > Calendar.getInstance().timeInMillis) {
                    if (event.status != EventStatusType.ONGOING.code) {
                        _eventStatusChanged.value = EventStatusType.ONGOING.code
                    }
                } else {
                    if (event.status != EventStatusType.ENDED.code) {
                        _eventStatusChanged.value = EventStatusType.ENDED.code
                    }
                }
            }
            else -> {}
        }
    }

    fun updateEventStatus(newStatus: Int) {
        coroutineScope.launch {

            when (
                val result = repository.updateEventStatus(
                    docId = onEventDisplaying.value!!.id,
                    code = newStatus
                )
            ) {
                is Result.Success -> {
                    _error.value = null
                    onStatusChangedLog(newStatus)
                }
                is Result.Fail -> {
                    _error.value = result.error
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                }
                else -> {
                    _error.value = getString(R.string.result_fail)
                }
            }
        }
    }

    private fun onStatusChangedLog(status: Int) {

        val log = PostLog(
            postDocId = onEventDisplaying.value!!.id,
            operatorUid = userInfo!!.uid
        )
        when (status) {
            EventStatusType.ONGOING.code -> {
                log.logType = LogType.EVENT_STARTED.value
                log.logMsg = getStringWithStrParm(
                    R.string.log_msg_event_status_ongoing,
                    onEventDisplaying.value!!.title
                )
            }
            EventStatusType.ENDED.code -> {
                log.logType = LogType.EVENT_ENDED.value
                log.logMsg = getStringWithStrParm(
                    R.string.log_msg_event_status_ended,
                    onEventDisplaying.value!!.title
                )
            }
        }
        saveLog(log)
    }

    fun onNavigateToTargetProfile(uid: String) {
        var target = UserInfo()
        target.uid = uid

        _targetUser.value = target
    }

    fun navigateToProfileComplete() {
        _targetUser.value = null
    }
}
