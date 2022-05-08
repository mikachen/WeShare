package com.zoe.weshare.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.GiftStatusType
import com.zoe.weshare.util.LogType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: WeShareRepository) : ViewModel() {

    var allLogs = MutableLiveData<List<OperationLog>>()
    var filteredLogs = MutableLiveData<List<OperationLog>>()

    private var _gifts = MutableLiveData<List<GiftPost>>()
    val gifts: LiveData<List<GiftPost>>
        get() = _gifts

    private var _events = MutableLiveData<List<EventPost>>()
    val events: LiveData<List<EventPost>>
        get() = _events

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    private val _refreshStatus = MutableLiveData<Boolean>()
    val refreshStatus: LiveData<Boolean>
        get() = _refreshStatus

    private val _navigateToSelectedGift = MutableLiveData<GiftPost?>()
    val navigateToSelectedGift: LiveData<GiftPost?>
        get() = _navigateToSelectedGift

    private val _navigateToSelectedEvent = MutableLiveData<EventPost?>()
    val navigateToSelectedEvent: LiveData<EventPost?>
        get() = _navigateToSelectedEvent

    var isGiftCardsComplete: Boolean = false
    var isEventCardsComplete: Boolean = false

    init {
        getGiftsResult()
        getEventsResult()
        getLiveAllLogsResult()
    }

    fun getLiveAllLogsResult() {
        allLogs = repository.getLiveLogs()
    }

    fun onFilteringLog(list: List<OperationLog>){
        list.filter { it.logType != LogType.REQUEST_GIFT.value}
        list.filter { it.logType != LogType.FOLLOWING.value}
        list.filter { it.logType != LogType.ABANDONED_GIFT.value}

        filteredLogs.value = list
    }

    private fun filterGift(gifts: List<GiftPost>){
        _gifts.value = gifts.filter { it.status != GiftStatusType.CLOSED.code }
    }

    private fun getGiftsResult() {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.getAllGifts()) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    filterGift(result.data)
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
            _refreshStatus.value = false
        }
    }

    private fun getEventsResult() {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING
            val result = repository.getAllEvents()

            _events.value = when (result) {
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
            _refreshStatus.value = false
        }
    }

    fun displayEventDetails(event: EventPost) {
        _navigateToSelectedEvent.value = event
    }

    fun displayEventDetailsComplete() {
        _navigateToSelectedEvent.value = null
    }

    fun displayGiftDetails(gift: GiftPost) {
        _navigateToSelectedGift.value = gift
    }

    fun displayGiftDetailsComplete() {
        _navigateToSelectedGift.value = null
    }
}
