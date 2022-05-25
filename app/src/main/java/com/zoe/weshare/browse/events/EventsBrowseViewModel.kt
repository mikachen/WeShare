package com.zoe.weshare.browse.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.data.Result
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.UserManager.userBlackList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class EventsBrowseViewModel(private val repository: WeShareRepository) : ViewModel() {

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

    var emptyQuery = MutableLiveData<Boolean?>()

    init {
        getALLEventsResult()
    }

    private fun getALLEventsResult() {
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
        }
    }


    /**
     * exclude the authors in user's black list
     * */
    private fun filterEvent(events: List<EventPost>) {
        val filteredList = events.filterNot {
            userBlackList.contains(it.author.uid) } as MutableList

        filteredList.sortByDescending { it.createdTime }

        _events.value = filteredList
    }

    /**
     * observe and show error animation when there's no item to display.
     * */
    fun onEmptyQuery(result: Boolean) {
        emptyQuery.value = result
    }

    fun onNavigateEventDetails(event: EventPost) {
        _navigateToSelectedEvent.value = event
    }

    fun onNavigateEventDetailsComplete() {
        _navigateToSelectedEvent.value = null
        emptyQuery.value = null
    }
}
