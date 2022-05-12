package com.zoe.weshare.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.data.OperationLog
import com.zoe.weshare.data.Result
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

    fun onFilteringLog(list: List<OperationLog>) {
        filteredLogs.value = list.filter {
            it.logType != LogType.REQUEST_GIFT.value &&
                    it.logType != LogType.FOLLOWING.value &&
                    it.logType != LogType.ABANDONED_GIFT.value &&
                    it.logType != LogType.VOLUNTEER_EVENT.value &&
                    it.logType != LogType.EVENT_GOT_FORCE_ENDED.value
        }
    }

    private fun filterGift(gifts: List<GiftPost>) {

        val list = gifts.filter {
            it.status != GiftStatusType.CLOSED.code && it.whoLiked.size >= 2 } as MutableList

        list.sortByDescending { it.whoLiked.size }

        _gifts.value = list
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

    private fun filterEvent(event: List<EventPost>) {
        _events.value = event.filter { it.whoAttended.size >= 4 }
    }

    private fun getEventsResult() {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.getAllEvents()) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    filterEvent(result.data)
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
