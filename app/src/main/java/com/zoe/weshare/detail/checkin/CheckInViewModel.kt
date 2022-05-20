package com.zoe.weshare.detail.checkin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.data.OperationLog
import com.zoe.weshare.data.Result
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.Const.FIELD_EVENT_CHECKED_IN
import com.zoe.weshare.util.Const.PATH_EVENT_POST
import com.zoe.weshare.util.LogType
import com.zoe.weshare.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CheckInViewModel(
    private val repository: WeShareRepository,
    private val userInfo: UserInfo,
) : ViewModel() {

    private lateinit var event: EventPost

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _saveLogComplete = MutableLiveData<OperationLog?>()
    val saveLogComplete: LiveData<OperationLog?>
        get() = _saveLogComplete

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    fun fetchArg(arg: EventPost) {
        event = arg
    }

    fun checkInEvent(doc: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.updateFieldValue(
                    collection = PATH_EVENT_POST,
                    docId = doc,
                    field = FIELD_EVENT_CHECKED_IN,
                    value = FieldValue.arrayUnion(userInfo.uid)
                )
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    onSaveLog(doc)
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

    fun onSaveLog(doc: String) {
        val log = OperationLog(
            logType = LogType.EVENT_CHECK_IN.value,
            logMsg = WeShareApplication.instance.getString(
                R.string.log_msg_event_check_in, userInfo.name, event.title),
            postDocId = doc,
            operatorUid = userInfo.uid
        )
        saveLog(log)
    }

    private fun saveLog(log: OperationLog) {
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
                    _error.value = Util.getString(R.string.result_fail)
                }
            }
        }
    }

    fun checkInComplete() {
        _saveLogComplete.value = null
    }
}
