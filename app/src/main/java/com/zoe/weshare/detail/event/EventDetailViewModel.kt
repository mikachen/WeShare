package com.zoe.weshare.detail.event

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
import com.zoe.weshare.util.Const.FIELD_USER_BLACKLIST
import com.zoe.weshare.util.Const.FIELD_WHO_LIKED
import com.zoe.weshare.util.Const.PATH_EVENT_POST
import com.zoe.weshare.util.Const.PATH_USER
import com.zoe.weshare.util.Const.SUB_PATH_EVENT_USER_WHO_COMMENT
import com.zoe.weshare.util.EventStatusType
import com.zoe.weshare.util.LogType
import com.zoe.weshare.util.UserManager.userBlackList
import com.zoe.weshare.util.Util.getString
import com.zoe.weshare.util.Util.getStringWithStrParm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class EventDetailViewModel(private val repository: WeShareRepository, val userInfo: UserInfo) :
    ViewModel() {

    private lateinit var event: EventPost

    var liveEventDetailResult = MutableLiveData<EventPost?>()
    var liveComments = MutableLiveData<List<Comment>>()

    private var _filteredComments = MutableLiveData<List<Comment>>()
    val filteredComments: LiveData<List<Comment>>
        get() = _filteredComments

    private var _onTargetAvatarClicked = MutableLiveData<UserInfo?>()
    val onTargetAvatarClicked: LiveData<UserInfo?>
        get() = _onTargetAvatarClicked

    private var _eventStatusChanged = MutableLiveData<Int?>()
    val eventStatusChanged: LiveData<Int?>
        get() = _eventStatusChanged

    private var _onNavigateToRoom = MutableLiveData<ChatRoom?>()
    val onNavigateToRoom: LiveData<ChatRoom?>
        get() = _onNavigateToRoom

    val userProfileList = mutableListOf<UserProfile>()

    private var onSearchUserProfile = MutableLiveData<Int>()

    private var _profileSearchComplete = MutableLiveData<Boolean>()
    val profileSearchComplete: LiveData<Boolean>
        get() = _profileSearchComplete

    var onBlockListUser = MutableLiveData<UserProfile?>()

    var onReportUserViolation = MutableLiveData<String?>()

    private val _userAttendType = MutableLiveData<String?>()
    val userAttendType: LiveData<String?>
        get() = _userAttendType

    private val _saveLogComplete = MutableLiveData<OperationLog?>()
    val saveLogComplete: LiveData<OperationLog?>
        get() = _saveLogComplete

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    init {
        _profileSearchComplete.value = false
    }

    fun onViewPrepare(selectedEvent: EventPost) {
        getLiveEventDetail(selectedEvent)
        getLiveComments(selectedEvent)
    }

    private fun getLiveEventDetail(event: EventPost) {
        liveEventDetailResult = repository.getLiveEventDetail(docId = event.id)
    }

    private fun getLiveComments(event: EventPost) {
        liveComments = repository.getLiveComments(
            collection = PATH_EVENT_POST,
            docId = event.id,
            subCollection = SUB_PATH_EVENT_USER_WHO_COMMENT
        ) }

    fun excludeBlackListUser() {
        _filteredComments.value =
            liveComments.value?.filterNot { userBlackList.contains(it.uid) }
    }

    fun fetchEvent(event:EventPost){
        this.event = event
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


    /**
     * in oder to show user's avatar image and user's name,
     * this will decide if the user profile has been queried before,
     * and only sort out the new user profile that we never get
     * */

    fun onGetUsersProfile(comments: List<Comment>) {

        if (comments.isNotEmpty()) {
            _profileSearchComplete.value = false

            val excludedSameUserComments = comments.distinctBy { it.uid }
            val newUserUid = mutableListOf<String>()

            if (userProfileList.isNotEmpty()) {

                for (comment in excludedSameUserComments) {
                    if (alreadyGotUserProfile(comment)) {

                        newUserUid.add(comment.uid)
                    }
                }
            } else {
                // first entry is always empty
                for (comment in excludedSameUserComments) {
                    newUserUid.add(comment.uid)
                }
            }

            if (newUserUid.size == 0) {
                _profileSearchComplete.value = true

            } else {
                onSearchUserProfile.value = newUserUid.size

                for (uid in newUserUid) {
                    getUsersProfile(uid)
                }
            }
        }
    }

    private fun alreadyGotUserProfile(comment: Comment): Boolean {
        return !userProfileList.any { it.uid == comment.uid }
    }

    private fun getUsersProfile(uid: String) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (val result = repository.getUserInfo(uid)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    val profile = result.data

                    profile?.let {
                        userProfileList.add(profile)
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
                        getString(R.string.result_fail)
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
        return profileSearchComplete.value == true && userProfileList.isNotEmpty()
    }

    fun onPostLikePressed() {
        liveEventDetailResult.value?.let {
            if (!hasUserLikedBefore(it.whoLiked)) {
                sendLike(it.id)
            } else {
                cancelLike(it.id)
            }
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
                    value = FieldValue.arrayUnion(userInfo.uid)
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
                    value = FieldValue.arrayRemove(userInfo.uid)
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
            uid = userInfo.uid,
            content = message
        )
        sendComment(newComment)
    }

    fun sendComment(comment: Comment) {
        coroutineScope.launch {

            when (
                val result = repository.sendComment(
                    collection = PATH_EVENT_POST,
                    docId = event.id,
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
                            userInfo.name,
                            event.title
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
            postDocId = event.id,
            operatorUid = userInfo.uid
        )
        saveLog(log)
    }

    private fun saveLog(log: OperationLog) {
        coroutineScope.launch {

            when (val result = repository.saveLog(log)) {
                is Result.Success -> {
                    _error.value = null
                    _saveLogComplete.value = log

                    _eventStatusChanged.value = null
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
                val result = repository.updateSubCollectionFieldValue(
                    collection = PATH_EVENT_POST,
                    docId = event.id,
                    subCollection = SUB_PATH_EVENT_USER_WHO_COMMENT,
                    subDocId = subDoc,
                    field = FIELD_WHO_LIKED,
                    value = FieldValue.arrayUnion(userInfo.uid)
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
                val result = repository.updateSubCollectionFieldValue(
                    collection = PATH_EVENT_POST,
                    docId = event.id,
                    subCollection = SUB_PATH_EVENT_USER_WHO_COMMENT,
                    subDocId = subDoc,
                    field = FIELD_WHO_LIKED,
                    value = FieldValue.arrayRemove(userInfo.uid)
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
                    docId = event.id,
                    field = fieldString,
                    value = FieldValue.arrayUnion(userInfo.uid)
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
                    docId = event.id,
                    field = fieldString,
                    value = FieldValue.arrayRemove(userInfo.uid)
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
                    repository.getEventRoom(docId = event.roomId)
            ) {
                is Result.Success -> {
                    _error.value = null

                    val room = result.data

                    checkRoomParticipants(room)
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

    fun checkRoomParticipants(room: ChatRoom) {

        if (userJoinedRoomBefore(room)) {

            _onNavigateToRoom.value = room

        } else {
            updateRoomParticipants(room)
        }
    }

    private fun userJoinedRoomBefore(room: ChatRoom): Boolean {
        return room.participants.contains(userInfo.uid)
    }

    fun updateRoomParticipants(room: ChatRoom) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.updateEventRoom(
                    roomId = event.roomId,
                    user = userInfo
                )
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    _onNavigateToRoom.value = room
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

    fun updateEventStatus(newStatus: Int) {
        coroutineScope.launch {

            when (
                val result = repository.updateEventStatus(
                    docId = event.id,
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
            postDocId = event.id,
            operatorUid = userInfo.uid
        )

        when (status) {
            EventStatusType.ONGOING.code -> {
                log.logType = LogType.EVENT_STARTED.value
                log.logMsg = getStringWithStrParm(
                    R.string.log_msg_event_status_ongoing,
                    event.title
                )
            }

            EventStatusType.ENDED.code -> {
                log.logType = LogType.EVENT_ENDED.value
                log.logMsg = getStringWithStrParm(
                    R.string.log_msg_event_status_ended,
                    event.title
                )
            }
        }
        saveLog(log)
    }

    fun onNavigateToTargetProfile(uid: String) {
        val target = UserInfo()
        target.uid = uid

        _onTargetAvatarClicked.value = target
    }

    fun blockUser(target: UserProfile) {
        _status.value = LoadApiStatus.LOADING

        coroutineScope.launch {
            when (
                val result = repository.updateFieldValue(
                    collection = PATH_USER,
                    docId = userInfo.uid,
                    field = FIELD_USER_BLACKLIST,
                    value = FieldValue.arrayUnion(target.uid)
                )
            ) {
                is Result.Success -> {

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

    fun refreshCommentBoard() {
        excludeBlackListUser()

        onBlockListUser.value = null
    }

    //consider the case if get profile fail, return null
    fun getSpeakerProfile(comment: Comment): UserProfile? {
        return userProfileList.singleOrNull { it.uid == comment.uid }
    }

    fun navigateToProfileComplete() {
        _onTargetAvatarClicked.value = null
    }

    fun saveLogComplete() {
        _userAttendType.value = null
        _saveLogComplete.value = null
    }

    fun navigateToRoomComplete() {
        _onNavigateToRoom.value = null
    }

    fun onNavigateToReportDialog(target: UserProfile) {
        onReportUserViolation.value = target.uid
    }

    fun navigateToReportDialogComplete() {
        onReportUserViolation.value = null
    }
}
