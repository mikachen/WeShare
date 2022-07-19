package com.zoe.weshare.manage.eventItem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.EventStatusType
import com.zoe.weshare.util.LogType
import com.zoe.weshare.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class EventManageViewModel(
    private val repository: WeShareRepository,
    private val userInfo: UserInfo
) : ViewModel() {

    private val _allEventsResult = MutableLiveData<List<EventPost>>()
    val allEventsResult: LiveData<List<EventPost>>
        get() = _allEventsResult

    private val _qrcode = MutableLiveData<String>()
    val qrcode: LiveData<String>
        get() = _qrcode

    var onFilterEmpty = MutableLiveData<Boolean>()
    var firstEntryEmpty = MutableLiveData<Boolean>()

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    private val _saveLogComplete = MutableLiveData<OperationLog>()
    val saveLogComplete: LiveData<OperationLog>
        get() = _saveLogComplete

    private val _onAlterMsgShowing = MutableLiveData<EventPost>()
    val onAlterMsgShowing: LiveData<EventPost>
        get() = _onAlterMsgShowing

    init {
        getUserAllEventsPosts()
    }

    fun getUserAllEventsPosts() {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.getUserAllEventsPosts(uid = userInfo.uid)
            ) {

                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    _allEventsResult.value = result.data ?: emptyList()
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
        }
    }

    fun userClickForceEnd(event: EventPost) {
        _onAlterMsgShowing.value = event
    }

    fun forceEndedEvent(event: EventPost) {
        coroutineScope.launch {

            when (
                val result = repository.updateEventStatus(
                    docId = event.id,
                    code = EventStatusType.ENDED.code
                )
            ) {
                is Result.Success -> {
                    _error.value = null

                    onStatusChangedLog(event)
                    _onAlterMsgShowing.value = null
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

    private fun onStatusChangedLog(event: EventPost) {
        val log = OperationLog(
            postDocId = event.id,
            operatorUid = userInfo.uid,
            logType = LogType.EVENT_GOT_FORCE_ENDED.value,
            logMsg = WeShareApplication.instance
                .getString(R.string.log_msg_force_end_event, userInfo.name, event.title)
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

    fun generateQrcode(eventId: String) {
        _qrcode.value = eventId
    }

    fun generateQrcodeComplete() {
        _qrcode.value = null
    }

    fun refreshFilterView() {
        getUserAllEventsPosts()
    }
}
