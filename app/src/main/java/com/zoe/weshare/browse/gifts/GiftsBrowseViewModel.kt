package com.zoe.weshare.browse.gifts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.data.Result
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.GiftStatusType
import com.zoe.weshare.util.UserManager.userBlackList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GiftsBrowseViewModel(private val repository: WeShareRepository) : ViewModel() {

    private var _gifts = MutableLiveData<List<GiftPost>>()
    val gifts: LiveData<List<GiftPost>>
        get() = _gifts

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    private val _navigateToSelectedGift = MutableLiveData<GiftPost?>()
    val navigateToSelectedGift: LiveData<GiftPost?>
        get() = _navigateToSelectedGift

    var emptyQuery = MutableLiveData<Boolean?>()

    init {
        getGiftsResult()
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
        }
    }

    /**
     * exclude the authors in user's black list and CLOSED gifts
     * */
    private fun filterGift(gifts: List<GiftPost>) {

        val filteredList = gifts.filterNot {
            userBlackList.contains(it.author.uid)
                    || it.status == GiftStatusType.CLOSED.code } as MutableList

        filteredList.sortByDescending { it.whoLiked.size }

        _gifts.value = filteredList
    }

    fun onNavigateGiftDetails(event: GiftPost) {
        _navigateToSelectedGift.value = event
    }

    fun onNavigateGiftDetailsComplete() {
        _navigateToSelectedGift.value = null
        emptyQuery.value = null
    }

    fun onEmptyQuery(result: Boolean) {
        emptyQuery.value = result
    }
}
