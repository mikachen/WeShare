package com.zoe.weshare.message.roomlist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.Author
import com.zoe.weshare.data.ChatRoom
import com.zoe.weshare.data.Result
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RoomListViewModel(private val repository: WeShareRepository, private val author: Author?) : ViewModel() {

    private var _rooms = MutableLiveData<List<ChatRoom>>()
    val room: LiveData<List<ChatRoom>>
        get() = _rooms

    private var _navigateToSelectedRoom = MutableLiveData<ChatRoom>()
    val navigateToSelectedRoom: LiveData<ChatRoom>
        get() = _navigateToSelectedRoom

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // status: The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    // error: The internal MutableLiveData that stores the error of the most recent request
    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    private val _leave = MutableLiveData<Boolean>()
    val leave: LiveData<Boolean>
        get() = _leave


    init {
        author?.let { getRelatedChatRooms(it.uid) }
    }


    private fun getRelatedChatRooms(uid:String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            val result = repository.getRelatedChatRooms(uid)

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