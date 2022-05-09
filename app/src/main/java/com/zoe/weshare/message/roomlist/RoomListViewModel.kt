package com.zoe.weshare.message.roomlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.ChatRoom
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
) :
    ViewModel() {

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

    fun searchChatRooms() {
        userInfo?.let { getUserChatRooms(it.uid) }
    }

    private fun getUserChatRooms(uid: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            val result = repository.getUserChatRooms(uid)

            _rooms.value = when (result) {
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

    fun displayRoomDetails(selectedRoom: ChatRoom) {
        _navigateToSelectedRoom.value = selectedRoom
    }

    fun displayRoomDetailsComplete() {
        _navigateToSelectedRoom.value = null
    }
}
