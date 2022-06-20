package com.zoe.weshare.message

import android.icu.util.Calendar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.Const.FIELD_MESSAGE_WHO_READ
import com.zoe.weshare.util.Const.PATH_CHATROOM
import com.zoe.weshare.util.Const.SUB_PATH_CHATROOM_MESSAGE
import com.zoe.weshare.util.Logger
import com.zoe.weshare.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChatRoomViewModel(
    private val repository: WeShareRepository,
    private val userInfo: UserInfo
) : ViewModel() {

    var liveMessages = MutableLiveData<List<MessageItem>>()

    var msgDisplay = MutableLiveData<List<MessageItem>>()

    lateinit var chatRoom: ChatRoom

    private var _navigateToTargetUser = MutableLiveData<UserInfo?>()
    val navigateToTargetUser: LiveData<UserInfo?>
        get() = _navigateToTargetUser

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    //default 0
    var loopSize: Int = 0

    fun setRoomAndMessages(room: ChatRoom) {
        chatRoom = room
        liveMessages = repository.getLiveMessages(docId = room.id)
    }

    fun getUnReadItems(messages: List<MessageItem>) {

        val unReadItems = messages.filter {
            it is MessageItem.OnReceiveSide &&
                    it.message?.whoRead?.contains(userInfo.uid) == false
        }

        if (unReadItems.isEmpty()) {

            msgDisplay.value = messages

        } else {
            loopSize = unReadItems.size

            for (item in unReadItems) {
                item as MessageItem.OnReceiveSide

                item.message?.let {
                    updateMsgRead(it)
                }
            }
        }
    }

    /** update firebase read user array on each unRead  */
    private fun updateMsgRead(message: Comment) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.updateSubCollectionFieldValue(
                collection = PATH_CHATROOM,
                docId = chatRoom.id,
                subCollection = SUB_PATH_CHATROOM_MESSAGE,
                subDocId = message.id,
                field = FIELD_MESSAGE_WHO_READ,
                value = FieldValue.arrayUnion(userInfo.uid))
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

            loopSize -= 1

            if (loopSize == 0) {
                msgDisplay.value = liveMessages.value

                liveMessages.value?.let { getLastMsgItem(it) }
            }
        }
    }

    fun getLastMsgItem(messages: List<MessageItem>) {
        when (val item = messages.last()) {
            is MessageItem.OnSendSide -> item.message?.let { updateLastMsgReadUser(it) }
            is MessageItem.OnReceiveSide -> item.message?.let { updateLastMsgReadUser(it) }
        }
    }

    fun onSending(inputMsg: String) {

        val message = Comment(
            uid = userInfo.uid,
            content = inputMsg,
            createdTime = Calendar.getInstance().timeInMillis,
            whoRead = listOf(userInfo.uid)
        )

        sendNewMessage(chatRoom.id, message)
    }


    /** everytime sending a new msg out,
     * update LastMsgRecord(Message Collection) & LastMsgReadUser(ChatRoom Doc) */
    fun sendNewMessage(docId: String, message: Comment) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when ( val result = repository.sendMessage(docId, message)) {

                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    saveLastMsgRecord(docId,message)
                    updateLastMsgReadUser(message)
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

    private fun saveLastMsgRecord(docId: String, message: Comment) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (val result = repository.saveLastMsgRecord(docId, message)) {

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

    fun updateLastMsgReadUser(message: Comment) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when ( val result = repository.setLastMsgReadUser(
                docId = chatRoom.id,
                uidList = message.whoRead )
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

    fun onNavigateToTargetProfile(uid: String) {
        val target = UserInfo()
        target.uid = uid

        _navigateToTargetUser.value = target
    }

    fun navigateToProfileComplete() {
        _navigateToTargetUser.value = null
    }
}
