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
import com.zoe.weshare.util.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChatRoomViewModel(private val repository: WeShareRepository, private val userInfo: UserInfo?) :
    ViewModel() {

    private var messageRecords = mutableListOf<MessageItem>()

    private var _messageItems = MutableLiveData<List<MessageItem>?>()
    val messageItems: LiveData<List<MessageItem>?>
        get() = _messageItems

    private var _chatRoom = MutableLiveData<ChatRoom>()
    val chatRoom: LiveData<ChatRoom>
        get() = _chatRoom

    private var _targetInfo = MutableLiveData<UserInfo>()
    val targetInfo: LiveData<UserInfo>
        get() = _targetInfo

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
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error


    fun onSending(inputMsg: String) {
        _newMessage.value = Comment(
            uid = userInfo!!.uid,
            content = inputMsg,
            createdTime = Calendar.getInstance().timeInMillis
        )
    }


    fun getHistoryMessage(docId: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.getChatsHistory(docId)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE
                    _messageItems.value =  result.data
                    messageRecords = result.data as MutableList<MessageItem>
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _status.value = LoadApiStatus.ERROR
                    _messageItems.value = null
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _status.value = LoadApiStatus.ERROR
                    _messageItems.value = null
                }
                else -> {
                    _error.value =
                        WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                    _messageItems.value = null
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

                    saveLastMsgRecord(docId) //發成功才執行保存對話
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

    fun onUserInfoDisplay(chatRoom: ChatRoom) {
        _targetInfo.value = chatRoom.usersInfo?.single { it.uid != UserManager.userZoe.uid }
    }

    fun onNewMsgListened(list: MutableList<Comment>) {
        val newMsgItem = ChatsHistory(list).toMessageItem()
        messageRecords.addAll(newMsgItem)
        _messageItems.value = messageRecords
    }
}
