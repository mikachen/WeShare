package com.zoe.weshare.search.gifts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.data.Result
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GiftsAllViewModel(private val repository: WeShareRepository) : ViewModel() {

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

    var onSearchEmpty = MutableLiveData<Boolean>()

    init {
        getGiftsResult()
    }

    private fun getGiftsResult() {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING
            val result = repository.getAllGifts()

            _gifts.value = when (result) {
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

    fun displayGiftDetails(event: GiftPost) {
        _navigateToSelectedGift.value = event
    }

    fun displayGiftDetailsComplete() {
        _navigateToSelectedGift.value = null
        onSearchEmpty.value = null
    }

}