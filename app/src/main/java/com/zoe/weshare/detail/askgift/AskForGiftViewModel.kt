package com.zoe.weshare.detail.askgift

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
import com.zoe.weshare.util.LogType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AskForGiftViewModel(
    private val repository: WeShareRepository,
    private val userInfo: UserInfo?,
) : ViewModel() {

    private val _newRequestComment = MutableLiveData<Comment>()
    val newRequestComment: LiveData<Comment>
        get() = _newRequestComment

    private val _saveLogComplete = MutableLiveData<LoadApiStatus>()
    val saveLogComplete: LiveData<LoadApiStatus>
        get() = _saveLogComplete

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _requestGiftStatus = MutableLiveData<LoadApiStatus>()
    val requestGiftStatus: LiveData<LoadApiStatus>
        get() = _requestGiftStatus

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>?
        get() = _error

    fun onSendNewRequest(message: String, gift: GiftPost) {
        val comment = Comment(
            uid = userInfo!!.uid,
            content = message
        )
        _newRequestComment.value = comment
    }

    fun askForGiftRequest(gift: GiftPost, comment: Comment) {
        coroutineScope.launch {

            _requestGiftStatus.value = LoadApiStatus.LOADING

            when (val result = repository.sendComment(
                collection = PATH_GIFT_POST,
                docId = gift.id,
                comment = comment,
                subCollection = SUB_PATH_GIFT_USER_WHO_ASK_FOR)) {

                is Result.Success -> {
                    _error.value = null
                    _requestGiftStatus.value = LoadApiStatus.DONE

                    onSaveGiftRequestLog(gift)
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _requestGiftStatus.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _requestGiftStatus.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _requestGiftStatus.value = LoadApiStatus.ERROR
                }
            }
        }
    }


    private fun onSaveGiftRequestLog(gift: GiftPost) {
        val log = PostLog(
            postDocId = gift.id,
            logType = LogType.REQUEST_GIFT.value,
            operatorUid = userInfo!!.uid,
            logMsg = WeShareApplication.instance.getString(R.string.log_msg_request_gift,
                userInfo.name,
                gift.title
            )
        )
        saveGiftRequestLog(log)
    }

    private fun saveGiftRequestLog(log: PostLog) {
        coroutineScope.launch {

            _saveLogComplete.value = LoadApiStatus.LOADING

            when (val result = repository.saveLog(log)) {
                is Result.Success -> {
                    _error.value = null
                    _saveLogComplete.value = LoadApiStatus.DONE
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _saveLogComplete.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _saveLogComplete.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _saveLogComplete.value = LoadApiStatus.ERROR
                }
            }
        }
    }
}
