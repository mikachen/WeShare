package com.zoe.weshare.message

import android.icu.util.Calendar
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

class ChatRoomViewModel(
    private val repository: WeShareRepository,
    private val userInfo: UserInfo?
) : ViewModel() {

    var liveMessages = MutableLiveData<List<MessageItem>>()

    private var _chatRoom = MutableLiveData<ChatRoom>()
    val chatRoom: LiveData<ChatRoom>
        get() = _chatRoom

    var _newMessage = MutableLiveData<Comment>()
    val newMessage: LiveData<Comment>
        get() = _newMessage

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    fun getLiveMessageResult(room: ChatRoom) {
        liveMessages = repository.getLiveMessages(docId = room.id)
    }

    fun onSending(inputMsg: String) {
        _newMessage.value = Comment(
            uid = userInfo!!.uid,
            content = inputMsg,
            createdTime = Calendar.getInstance().timeInMillis
        )
    }

    fun sendNewMessage(docId: String, comment: Comment) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (val result = repository.sendMessage(docId, comment)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    saveLastMsgRecord(docId) // 發成功才執行保存對話
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

    private fun saveLastMsgRecord(docId: String) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (val result = repository.saveLastMsgRecord(docId, newMessage.value!!)) {
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

    fun onViewDisplay(chatRoom: ChatRoom) {

        _chatRoom.value = chatRoom

        getLiveMessageResult(chatRoom)
    }
}
