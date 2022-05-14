package com.zoe.weshare.message.roomlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.ChatRoom
import com.zoe.weshare.data.OperationLog
import com.zoe.weshare.data.Result
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RoomListViewModel(
    private val repository: WeShareRepository,
    val userInfo: UserInfo?
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

    // error: The internal MutableLiveData that stores the error of the most recent request
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    fun onViewDisplay(liveData: MutableLiveData<List<ChatRoom>>) {
        allRooms = liveData
    }

    fun orderByTime(rooms: List<ChatRoom>){
        rooms as MutableList
        rooms.sortByDescending { it.lastMsgSentTime }

        _rooms.value = rooms
    }

    fun displayRoomDetails(selectedRoom: ChatRoom) {
        _navigateToSelectedRoom.value = selectedRoom
    }

    fun displayRoomDetailsComplete() {
        _navigateToSelectedRoom.value = null
    }
}
