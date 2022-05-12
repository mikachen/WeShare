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
import com.zoe.weshare.util.Const.FIELD_USER_FOLLOWER
import com.zoe.weshare.util.Const.FIELD_USER_FOLLOWING
import com.zoe.weshare.util.Const.PATH_USER
import com.zoe.weshare.util.UserManager.weShareUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: WeShareRepository,
    val targetUser: UserInfo?,
) : ViewModel() {

    private var _user = MutableLiveData<UserProfile>()
    val user: LiveData<UserProfile>
        get() = _user

    private var _userLog = MutableLiveData<List<OperationLog>>()
    val userLog: LiveData<List<OperationLog>>
        get() = _userLog

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

    init {
        targetUser?.let {
            getUserInfo(it.uid)
            getUserLogs(it.uid)
        }
    }

    private fun getUserInfo(uid: String) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (val result = repository.getUserInfo(uid)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    _user.value = result.data!!
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

    private fun getUserLogs(uid: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.getUserLog(uid)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    _userLog.value = result.data ?: emptyList()
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

            when (
                val result = repository.updateFieldValue(
                    collection = PATH_USER,
                    docId = weShareUser!!.uid,
                    field = FIELD_USER_FOLLOWING,
                    value = FieldValue.arrayUnion(targetUid)
                )
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    onSaveFollowingLog()
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

            when (
                val result = repository.updateFieldValue(
                    collection = PATH_USER,
                    docId = weShareUser!!.uid,
                    field = FIELD_USER_FOLLOWING,
                    value = FieldValue.arrayRemove(targetUid)
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

            when (
                val result = repository.updateFieldValue(
                    collection = PATH_USER,
                    docId = targetUid,
                    field = FIELD_USER_FOLLOWER,
                    value = FieldValue.arrayUnion(weShareUser!!.uid)
                )
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
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun cancelTargetFollower(targetUid: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.updateFieldValue(
                    collection = PATH_USER,
                    docId = targetUid,
                    field = FIELD_USER_FOLLOWER,
                    value = FieldValue.arrayRemove(weShareUser!!.uid)
                )
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

    fun onSaveFollowingLog() {
        val log = OperationLog(
            postDocId = "none",
            logType = LogType.FOLLOWING.value,
            operatorUid = weShareUser!!.uid,
            logMsg = WeShareApplication.instance.getString(
                R.string.log_msg_following_someone,
                weShareUser!!.name,
                targetUser!!.name
            )
        )
        saveFollowingLog(log)
    }

    private fun saveFollowingLog(log: OperationLog) {
        coroutineScope.launch {

            when (val result = repository.saveLog(log)) {
                is Result.Success -> {
                    _error.value = null
                }
                is Result.Fail -> {
                    _error.value = result.error
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                }
                else -> {
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
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

        val result = rooms.filter {
            it.participants.contains(targetUser!!.uid) && it.type == ChatRoomType.PRIVATE.value
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
            participants = listOf(targetUser!!.uid, UserManager.weShareUser!!.uid),
            usersInfo = listOf(targetUser, UserManager.weShareUser!!)
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
}
