package com.zoe.weshare.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.data.OperationLog
import com.zoe.weshare.data.Result
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: WeShareRepository,
    private val userInfo: UserInfo?,
) : ViewModel() {

    var allMessage = MutableLiveData<List<OperationLog>>()

    private val _notifications = MutableLiveData<List<OperationLog>>()
    val notifications: LiveData<List<OperationLog>>
        get() = _notifications

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    fun filterList(position: Int) {
        when (position) {
            0 -> _notifications.value = allMessage.value?.filter { !it.read }
            1 -> _notifications.value = allMessage.value?.filter { it.read }
        }
    }

    fun onViewDisplay(liveData: MutableLiveData<List<OperationLog>>) {

        allMessage = liveData
        _notifications.value = allMessage.value?.filter { !it.read }
    }

    fun userOnClickAndRead(log: OperationLog) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.readNotification(
                    uid = userInfo!!.uid,
                    docId = log.id,
                    read = true
                )
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
}
