package com.zoe.weshare.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChatRoomViewModel(private val repository: WeShareRepository, private val author: Author?) :
    ViewModel() {

    val profileList = mutableListOf<UserProfile>()

    private var _messageItems = MutableLiveData<List<MessageItem>>()
    val messageItems: LiveData<List<MessageItem>>
        get() = _messageItems

    var _newMessage = MutableLiveData<Comment>()
    val newMessage: LiveData<Comment>
        get() = _newMessage

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

    private var _onProfileSearching = MutableLiveData<Int>()
    val onProfileSearching: LiveData<Int>
        get() = _onProfileSearching

    fun onSending(inputMsg: String) {
        _newMessage.value = Comment(
            uid = author!!.uid,
            content = inputMsg,
        )
    }

    fun getHistoryMessage(docId: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            val result = repository.getChatsHistory(docId)

            _messageItems.value = when (result) {
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

    fun sendNewMessage(docId: String, comment: Comment) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (val result = repository.sendMessage(docId, comment)) {
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

    fun getUserList(participants: List<String>) {
        _onProfileSearching.value = participants.size
        for (element in participants) {
            getUserInfo(element)
        }
    }

    private fun getUserInfo(uid: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.getUserInfo(uid)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE
                    profileList.add(result.data)
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
            _onProfileSearching.value = _onProfileSearching.value?.minus(1)
        }
    }
}
