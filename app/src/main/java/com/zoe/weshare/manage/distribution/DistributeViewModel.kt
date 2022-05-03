package com.zoe.weshare.manage.distribution

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.Const.PATH_GIFT_POST
import com.zoe.weshare.util.Const.SUB_PATH_GIFT_USER_WHO_ASK_FOR
import com.zoe.weshare.util.GiftStatusType
import com.zoe.weshare.util.LogType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DistributeViewModel(
    val repository: WeShareRepository,
    val userInfo: UserInfo?,
) : ViewModel() {

    lateinit var gift: GiftPost

    private var _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>>
        get() = _comments

    private var _onProfileSearchComplete = MutableLiveData<Int>()
    val onProfileSearchComplete: LiveData<Int>
        get() = _onProfileSearchComplete

    private val _onConfirmMsgShowing = MutableLiveData<UserProfile>()
    val onConfirmMsgShowing: LiveData<UserProfile>
        get() = _onConfirmMsgShowing

    val profileList = mutableListOf<UserProfile>()

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    private val _sendGiftStatus = MutableLiveData<LoadApiStatus>()
    val sendGiftStatus: LiveData<LoadApiStatus>
        get() = _sendGiftStatus

    private val _saveLogComplete = MutableLiveData<OperationLog>()
    val saveLogComplete: LiveData<OperationLog>
        get() = _saveLogComplete

    fun getAskForGiftComments(selectedGift: GiftPost) {
        gift = selectedGift
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.getAllComments(
                collection = PATH_GIFT_POST,
                docId = selectedGift.id,
                subCollection = SUB_PATH_GIFT_USER_WHO_ASK_FOR)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    _comments.value = result.data ?: emptyList()
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _status.value = LoadApiStatus.ERROR
                    _comments.value = emptyList()
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _status.value = LoadApiStatus.ERROR
                    _comments.value = emptyList()
                }
                else -> {
                    _error.value =
                        WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                    _comments.value = emptyList()
                }
            }
        }
    }

    fun searchUsersProfile(comments: List<Comment>?) {
        comments?.let {
            val filtered = comments.distinctBy { it.uid }
            _onProfileSearchComplete.value = filtered.size

            for (element in filtered) {
                getUsersInfo(element.uid)
            }
        }
    }

    private fun getUsersInfo(uid: String) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (val result = repository.getUserInfo(uid)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE
                    profileList.add(result.data!!)
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
            _onProfileSearchComplete.value = _onProfileSearchComplete.value?.minus(1)
        }
    }

    fun userPressSendGift(comment: Comment) {
        _onConfirmMsgShowing.value = profileList.single { it.uid == comment.uid }
    }

    fun sendGift(gift: GiftPost, user: UserProfile) {
        coroutineScope.launch {
            _sendGiftStatus.value = LoadApiStatus.LOADING

            when (
                val result =
                    repository.updateGiftStatus(gift.id, GiftStatusType.CLOSED.code, user.uid)
            ) {
                is Result.Success -> {
                    _error.value = null
                    _sendGiftStatus.value = LoadApiStatus.DONE

                    onSaveSendGiftLog()
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _sendGiftStatus.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _sendGiftStatus.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value =
                        WeShareApplication.instance.getString(R.string.result_fail)
                    _sendGiftStatus.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun onSaveSendGiftLog() {
        val log = OperationLog(
            postDocId = gift.id,
            logType = LogType.SEND_GIFT.value,
            operatorUid = userInfo!!.uid,
            logMsg = WeShareApplication.instance.getString(
                R.string.log_msg_send_away_gift,
                userInfo.name,
                gift.title,
                onConfirmMsgShowing.value?.name ?: ""
            )
        )
        saveSendGiftLog(log)
    }

    private fun saveSendGiftLog(log: OperationLog) {
        coroutineScope.launch {
            when (val result = repository.saveLog(log)) {
                is Result.Success -> {
                    _error.value = null

                    _saveLogComplete.value = log
                }
                is Result.Fail -> {
                    _error.value = result.error

                }
                is Result.Error -> {
                    _error.value = result.exception.toString()

                }
                else -> {
                    _error.value =
                        WeShareApplication.instance.getString(R.string.result_fail)

                }
            }
        }
    }
}
