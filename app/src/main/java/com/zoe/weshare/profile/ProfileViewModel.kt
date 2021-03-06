package com.zoe.weshare.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.*
import com.zoe.weshare.util.Const.FIELD_USER_BLACKLIST
import com.zoe.weshare.util.Const.FIELD_USER_FOLLOWER
import com.zoe.weshare.util.Const.FIELD_USER_FOLLOWING
import com.zoe.weshare.util.Const.PATH_USER
import com.zoe.weshare.util.UserManager.userBlackList
import com.zoe.weshare.util.UserManager.weShareUser
import kotlinx.coroutines.*

class ProfileViewModel(
    private val repository: WeShareRepository,
    val targetUser: UserInfo
) : ViewModel() {

    lateinit var userProfile: UserProfile

    private var _user = MutableLiveData<UserProfile>()
    val user: LiveData<UserProfile>
        get() = _user

    private var _userLog = MutableLiveData<List<OperationLog>>()
    val userLog: LiveData<List<OperationLog>>
        get() = _userLog

    private var _notificationMsg = MutableLiveData<OperationLog>()
    val notificationMsg: LiveData<OperationLog>
        get() = _notificationMsg

    private var _userChatRooms = MutableLiveData<List<ChatRoom>?>()
    val userChatRooms: LiveData<List<ChatRoom>?>
        get() = _userChatRooms


    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    private val _navigateToFormerRoom = MutableLiveData<ChatRoom?>()
    val navigateToFormerRoom: LiveData<ChatRoom?>
        get() = _navigateToFormerRoom

    private val _navigateToNewRoom = MutableLiveData<ChatRoom?>()
    val navigateToNewRoom: LiveData<ChatRoom?>
        get() = _navigateToNewRoom

    val onUpdateContribution = MutableLiveData<Contribution?>()

    var blockUserComplete = MutableLiveData<UserProfile>()

    init {
        getUserInfo(targetUser.uid)
    }

    private fun getUserInfo(uid: String) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (val result = repository.getUserInfo(uid)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    getUserLogs(uid)

                    val user = result.data

                    user?.let {
                        _user.value = it
                        userProfile = it
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

    fun getUserLogs(uid: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when ( val result = repository.getUserLog(uid)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    val logList = result.data
                    _userLog.value = logList
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _status.value = LoadApiStatus.ERROR
                    _userLog.value = emptyList()
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _status.value = LoadApiStatus.ERROR
                    _userLog.value = emptyList()
                }
                else -> {
                    _error.value =
                        WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                    _userLog.value = emptyList()
                }
            }
        }
    }

    /** when a "user" click someone follow,
     * A. update the target's follower value = "user"
     * */
    fun updateUserFollowing(targetUid: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when ( val result = repository.updateFieldValue(
                    collection = PATH_USER,
                    docId = weShareUser.uid,
                    field = FIELD_USER_FOLLOWING,
                    value = FieldValue.arrayUnion(targetUid))
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    updateTargetFollower(targetUid)
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

    fun cancelUserFollowing(targetUid: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.updateFieldValue(
                    collection = PATH_USER,
                    docId = weShareUser.uid,
                    field = FIELD_USER_FOLLOWING,
                    value = FieldValue.arrayRemove(targetUid))
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    cancelTargetFollower(targetUid)
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
                    _error.value = Util.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    /** when a "user" click someone follow,
     * B. update " user's " following value = "target"
     * */
    fun updateTargetFollower(targetUid: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.updateFieldValue(
                    collection = PATH_USER,
                    docId = targetUid,
                    field = FIELD_USER_FOLLOWER,
                    value = FieldValue.arrayUnion(weShareUser.uid))
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    getUserInfo(targetUid)
                    onSendNotification()
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

    fun cancelTargetFollower(targetUid: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.updateFieldValue(
                    collection = PATH_USER,
                    docId = targetUid,
                    field = FIELD_USER_FOLLOWER,
                    value = FieldValue.arrayRemove(weShareUser.uid))
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    getUserInfo(targetUid)
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
                    _error.value = Util.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    private fun onSendNotification() {
        val message = OperationLog(
            postDocId = "none",
            logType = LogType.FOLLOWING.value,
            operatorUid = weShareUser.uid,
            logMsg = WeShareApplication.instance.getString(
                R.string.log_msg_start_following_you,
                weShareUser.name,
            )
        )
        _notificationMsg.value = message
    }

    fun getUserAllRooms(user: UserInfo) {
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

        val result = rooms.filter {
            it.participants.contains(targetUser.uid) && it.type == ChatRoomType.PRIVATE.value
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
        val targetInfo: UserInfo
        val room: ChatRoom

        user.value?.let {

            targetInfo = UserInfo(
                name = it.name,
                image = it.image,
                uid = it.uid
            )

            room = ChatRoom(
                type = ChatRoomType.PRIVATE.value,
                participants = listOf(it.uid, weShareUser.uid),
                usersInfo = listOf(targetInfo, weShareUser)
            )

            createRoom(room)
        }
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

    fun calculateContribution(userLogs: List<OperationLog>) {

        val filterLogs = userLogs.filter {

            it.logType != LogType.EVENT_STARTED.value
                    && it.logType != LogType.EVENT_ENDED.value
                    && it.logType != LogType.ABANDONED_GIFT.value
                    && it.logType != LogType.FOLLOWING.value
                    && it.logType != LogType.EVENT_GOT_FORCE_ENDED.value
        }

        val totalContribution = user.value?.contribution?.totalContribution ?: 0
        var newValue = 0
        val enumLogType = enumValues<LogType>()

        for (log in filterLogs) {
            val type = enumLogType.single { it.value == log.logType }
            newValue += type.contribution
        }

        if (totalContribution < newValue) {

            val contribution = Contribution(
                totalContribution = newValue,
                giftPostsCount = filterLogs.filter { it.logType == LogType.POST_GIFT.value }.size,
                eventPostsCount = filterLogs.filter { it.logType == LogType.POST_EVENT.value }.size,
                sendGiftsCount = filterLogs.filter { it.logType == LogType.SEND_GIFT.value }.size,
                attendeesCount = filterLogs.filter { it.logType == LogType.ATTEND_EVENT.value }.size,
                volunteerCount = filterLogs.filter { it.logType == LogType.VOLUNTEER_EVENT.value }.size,
                checkInCount = filterLogs.filter { it.logType == LogType.EVENT_CHECK_IN.value }.size,
                commentsCount = filterLogs.filter { it.logType == LogType.COMMENT_EVENT.value }.size,
                requestGiftsCount = filterLogs.filter { it.logType == LogType.REQUEST_GIFT.value }.size
            )
            onUpdateContribution.value = contribution
        }
    }

    fun updateContribution(contribution: Contribution) {
        _status.value = LoadApiStatus.LOADING

        coroutineScope.launch {
            when ( val result = repository.updateUserContribution(
                    uid = targetUser.uid,
                    contribution = contribution)
            ){
                is Result.Success -> {
                    _status.value = LoadApiStatus.DONE
                    onUpdateContribution.value = null
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

    fun blockThisUser(target: UserProfile) {
            _status.value = LoadApiStatus.LOADING

            coroutineScope.launch {
                when (val result = repository.updateFieldValue(
                        collection = PATH_USER,
                        docId = weShareUser.uid,
                        field = FIELD_USER_BLACKLIST,
                        value = FieldValue.arrayUnion(target.uid))
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
    fun onNavigateToEditInfoPage(){

    }
}
