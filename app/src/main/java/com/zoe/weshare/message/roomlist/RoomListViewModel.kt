package com.zoe.weshare.message.roomlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.zoe.weshare.R
import com.zoe.weshare.data.ChatRoom
import com.zoe.weshare.data.Result
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.ChatRoomType
import com.zoe.weshare.util.Const.FIELD_ROOM_PARTICIPANTS
import com.zoe.weshare.util.Const.PATH_CHATROOM
import com.zoe.weshare.util.UserManager.weShareUser
import com.zoe.weshare.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RoomListViewModel(
    private val repository: WeShareRepository,
    val userInfo: UserInfo?,
) : ViewModel() {

    var allRooms = MutableLiveData<List<ChatRoom>>()

    private var _rooms = MutableLiveData<List<ChatRoom>>()
    val room: LiveData<List<ChatRoom>>
        get() = _rooms


    private var _navigateToSelectedRoom = MutableLiveData<ChatRoom?>()
    val navigateToSelectedRoom: LiveData<ChatRoom?>
        get() = _navigateToSelectedRoom

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    var leaveRoomComplete = MutableLiveData<ChatRoom>()


    fun onViewDisplay(liveData: MutableLiveData<List<ChatRoom>>) {
        allRooms = liveData
    }

    fun displayRoomDetails(selectedRoom: ChatRoom) {
        _navigateToSelectedRoom.value = selectedRoom
    }

    fun displayRoomDetailsComplete() {
        _navigateToSelectedRoom.value = null
    }

    fun onLeaveRoom(room: ChatRoom) {
        if (room.participants.size == 1) {
            when(room.type){
                ChatRoomType.MULTIPLE.value -> leaveChatRoom(room)
                ChatRoomType.PRIVATE.value -> removeChatRoom(room)
            }
        } else if (room.participants.size > 1) {
            leaveChatRoom(room)
        }
    }

    fun removeChatRoom(room: ChatRoom) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.removeDocument(
                    collection = PATH_CHATROOM,
                    docId = room.id,
                )
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    leaveRoomComplete.value = room
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

    fun leaveChatRoom(room: ChatRoom) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.updateFieldValue(
                    collection = PATH_CHATROOM,
                    docId = room.id,
                    field = FIELD_ROOM_PARTICIPANTS,
                    value = FieldValue.arrayRemove(userInfo!!.uid)
                )
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    leaveRoomComplete.value = room
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
}
