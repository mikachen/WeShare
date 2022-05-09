package com.zoe.weshare.search.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.data.Result
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class EventsAllViewModel(private val repository: WeShareRepository) : ViewModel() {

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

    private val _navigateToSelectedEvent = MutableLiveData<EventPost?>()
    val navigateToSelectedEvent: LiveData<EventPost?>
        get() = _navigateToSelectedEvent

    var onSearchEmpty = MutableLiveData<Boolean>()

    init {
        getALLEventsResult()
    }

    private fun getALLEventsResult() {
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
        }
    }

    fun onNavigateEventDetails(event: EventPost) {
        _navigateToSelectedEvent.value = event
    }

    fun onNavigateEventDetailsComplete() {
        _navigateToSelectedEvent.value = null
        onSearchEmpty.value = null
    }
}
