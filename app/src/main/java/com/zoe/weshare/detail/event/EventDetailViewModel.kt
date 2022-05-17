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
import com.zoe.weshare.util.Const
import com.zoe.weshare.util.Const.FIELD_WHO_LIKED
import com.zoe.weshare.util.Const.PATH_EVENT_POST
import com.zoe.weshare.util.Const.SUB_PATH_EVENT_USER_WHO_COMMENT
import com.zoe.weshare.util.EventStatusType
import com.zoe.weshare.util.LogType
import com.zoe.weshare.util.UserManager
import com.zoe.weshare.util.Util.getString
import com.zoe.weshare.util.Util.getStringWithStrParm
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class EventDetailViewModel(private val repository: WeShareRepository, val userInfo: UserInfo?) :
    ViewModel() {

    var liveEventDetailResult = MutableLiveData<EventPost?>()
    var liveComments = MutableLiveData<List<Comment>>()

    private var _filteredComments = MutableLiveData<List<Comment>>()
    val filteredComments: LiveData<List<Comment>>
        get() = _filteredComments

    private var _room = MutableLiveData<ChatRoom?>()
    val room: LiveData<ChatRoom?>
        get() = _room

    private var _targetUser = MutableLiveData<UserInfo>()
    val targetUser: LiveData<UserInfo>
        get() = _targetUser

    private var _statusTriggerChanged = MutableLiveData<Int>()
    val statusTriggerChanged: LiveData<Int>
        get() = _statusTriggerChanged

    private var isStatusChecked: Boolean = false

    private var _onNavigateToRoom = MutableLiveData<ChatRoom?>()
    val onNavigateToRoom: LiveData<ChatRoom?>
        get() = _onNavigateToRoom

    private val _refreshStatus = MutableLiveData<Boolean>()
    val refreshStatus: LiveData<Boolean>
        get() = _refreshStatus

    private var _onProfileSearchComplete = MutableLiveData<Int>()
    val onProfileSearchComplete: LiveData<Int>
        get() = _onProfileSearchComplete

    val profileList = mutableListOf<UserProfile>()

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val _saveLogComplete = MutableLiveData<OperationLog>()
    val saveLogComplete: LiveData<OperationLog>
        get() = _saveLogComplete

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _updateRoomStatus = MutableLiveData<LoadApiStatus?>()
    val updateRoomStatus: LiveData<LoadApiStatus?>
        get() = _updateRoomStatus

    private val _userAttendType = MutableLiveData<String>()
    val userAttendType: LiveData<String>
        get() = _userAttendType

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    var blockUserComplete = MutableLiveData<UserProfile>()

    private var _reportedTarget = MutableLiveData<String>()
    val reportedTarget: LiveData<String>
        get() = _reportedTarget

    fun onViewPrepare(event: EventPost) {
        getLiveEventDetail(event)
        getLiveComments(event)
    }

    private fun getLiveEventDetail(event: EventPost) {
        liveEventDetailResult = repository.getLiveEventDetail(docId = event.id)
    }

    private fun getLiveComments(event: EventPost) {
        liveComments = repository.getLiveComments(
            collection = PATH_EVENT_POST,
            docId = event.id,
            subCollection = SUB_PATH_EVENT_USER_WHO_COMMENT
        )
    }

    fun filterComment() {
        _filteredComments.value =
            liveComments.value?.filterNot { UserManager.userBlackList.contains(it.uid) }
    }

    fun onGetUsersProfile(comments: List<Comment>) {

        if (comments.isNotEmpty()) {
            val filteredUser = comments.distinctBy { it.uid }
            val newUserUid = mutableListOf<String>()

            if (profileList.isNotEmpty()) {
                for (i in filteredUser) {
                    if (!profileList.any { it.uid == i.uid }) {
                        newUserUid.add(i.uid)
                    }
                }
            }
            // first time is always empty
            else {
                for (i in filteredUser) {
                    newUserUid.add(i.uid)
                }
            }

            _onProfileSearchComplete.value = newUserUid.size

            for (uid in newUserUid) {
                getUsersProfile(uid)
            }
        }
    }

    private fun getUsersProfile(uid: String) {
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
            _onProfileSearchComplete.value = _onProfileSearchComplete.value?.minus(1)
        }
    }

    fun onPostLikePressed(doc: String, isUserLiked: Boolean) {
        if (!isUserLiked) {
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
                    collection = PATH_EVENT_POST,
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

    fun onSendNewComment(message: String) {
        val newComment = Comment(
            uid = userInfo!!.uid,
            content = message
        )
        sendComment(newComment)
    }

    fun sendComment(comment: Comment) {
        coroutineScope.launch {

            when (
                val result = repository.sendComment(
                    collection = PATH_EVENT_POST,
                    docId = liveEventDetailResult.value!!.id,
                    comment = comment,
                    subCollection = SUB_PATH_EVENT_USER_WHO_COMMENT
                )
            ) {
                is Result.Success -> {
                    _error.value = null

                    onSaveLog(
                        logType = LogType.COMMENT_EVENT.value,
                        logMsg = WeShareApplication.instance.getString(
                            R.string.log_msg_send_event_comment,
                            userInfo!!.name,
                            liveEventDetailResult.value!!.title
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

    fun onSaveLog(logType: Int, logMsg: String) {
        val log = OperationLog(
            logType = logType,
            logMsg = logMsg,
            postDocId = liveEventDetailResult.value!!.id,
            operatorUid = userInfo!!.uid
        )
        saveLog(log)
    }

    private fun saveLog(log: OperationLog) {
        coroutineScope.launch {

            when (val result = repository.saveLog(log)) {
                is Result.Success -> {
                    _error.value = null
                    _saveLogComplete.value = log

                    _statusTriggerChanged.value = null
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
                    docId = liveEventDetailResult.value!!.id,
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
                    docId = liveEventDetailResult.value!!.id,
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

            when (
                val result = repository.updateFieldValue(
                    collection = PATH_EVENT_POST,
                    docId = liveEventDetailResult.value!!.id,
                    field = fieldString,
                    value = FieldValue.arrayUnion(userInfo!!.uid)
                )
            ) {
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

    fun cancelAttendEvent(fieldString: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.updateFieldValue(
                    collection = PATH_EVENT_POST,
                    docId = liveEventDetailResult.value!!.id,
                    field = fieldString,
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
                    _error.value = getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    /**
     * when user click on enter room :
     * 1) getEventRoom
     * 2) check if user has been in chat before
     * 3) if true -> navigate to room
     * 4) if false -> update room doc -> navigate to room
     */
    fun getChatRoomInfo() {
        coroutineScope.launch {
            when (
                val result =
                    repository.getEventRoom(docId = liveEventDetailResult.value!!.roomId)
            ) {
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

    fun checkUserInRoomBefore(room: ChatRoom) {

        if (room.participants.isNotEmpty()) {
            if (room.participants.contains(userInfo!!.uid)) {

                _onNavigateToRoom.value = room
            } else {
                // a new user never been in room before
                updateRoomParticipants(room)
            }
        } else {
            // the first user want to enter this room
            updateRoomParticipants(room)
        }
    }

    fun updateRoomParticipants(room: ChatRoom) {
        coroutineScope.launch {
            _updateRoomStatus.value = LoadApiStatus.LOADING

            when (
                val result = repository.updateEventRoom(
                    roomId = liveEventDetailResult.value!!.roomId,
                    user = userInfo!!
                )
            ) {
                is Result.Success -> {
                    _error.value = null
                    _updateRoomStatus.value = LoadApiStatus.DONE

                    _onNavigateToRoom.value = room
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

    fun checkEventStatus(event: EventPost) {
        if (!isStatusChecked) {
            when (true) {
                (event.startTime > Calendar.getInstance().timeInMillis) -> {
                    if (event.status != EventStatusType.WAITING.code) {
                        _statusTriggerChanged.value = EventStatusType.WAITING.code

                        isStatusChecked = true
                    }
                }

                (event.startTime < Calendar.getInstance().timeInMillis) -> {
                    if (event.endTime > Calendar.getInstance().timeInMillis) {
                        if (event.status != EventStatusType.ONGOING.code) {
                            _statusTriggerChanged.value = EventStatusType.ONGOING.code

                            isStatusChecked = true
                        }
                    } else {
                        if (event.status != EventStatusType.ENDED.code) {
                            _statusTriggerChanged.value = EventStatusType.ENDED.code

                            isStatusChecked = true
                        }
                    }
                }
                else -> {
                    isStatusChecked = true
                }
            }
        }
    }

    fun updateEventStatus(newStatus: Int) {
        coroutineScope.launch {

            when (
                val result = repository.updateEventStatus(
                    docId = liveEventDetailResult.value!!.id,
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

        val log = OperationLog(
            postDocId = liveEventDetailResult.value!!.id,
            operatorUid = userInfo!!.uid
        )

        when (status) {
            EventStatusType.ONGOING.code -> {
                log.logType = LogType.EVENT_STARTED.value
                log.logMsg = getStringWithStrParm(
                    R.string.log_msg_event_status_ongoing,
                    liveEventDetailResult.value!!.title
                )
            }

            EventStatusType.ENDED.code -> {
                log.logType = LogType.EVENT_ENDED.value
                log.logMsg = getStringWithStrParm(
                    R.string.log_msg_event_status_ended,
                    liveEventDetailResult.value!!.title
                )
            }
        }
        saveLog(log)
    }

    fun onNavigateToTargetProfile(uid: String) {
        val target = UserInfo()
        target.uid = uid

        _targetUser.value = target
    }

    fun navigateToProfileComplete() {
        _targetUser.value = null
    }

    fun saveLogComplete() {
        _userAttendType.value = null
        _saveLogComplete.value = null
    }

    fun navigateToRoomComplete() {
        _updateRoomStatus.value = null
        _onNavigateToRoom.value = null
        _room.value = null
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

                    UserManager.userBlackList.add(target.uid)
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

    fun refreshCommentBoard() {

        filterComment()
        blockUserComplete.value = null
    }

    fun onNavigateToReportDialog(target: UserProfile) {
        _reportedTarget.value = target.uid
    }

    fun navigateToReportComplete(){
        _reportedTarget.value = null
    }
}
