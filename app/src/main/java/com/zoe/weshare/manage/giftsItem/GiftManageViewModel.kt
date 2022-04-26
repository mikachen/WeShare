package com.zoe.weshare.manage.giftsItem

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.Const.PATH_GIFT_POST
import com.zoe.weshare.util.GiftStatusType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GiftManageViewModel(
    private val repository: WeShareRepository,
    private val userInfo: UserInfo?,
) : ViewModel() {

    private var _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>>
        get() = _comments

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _searchGiftsStatus = MutableLiveData<LoadApiStatus>()
    val searchGiftsStatus: LiveData<LoadApiStatus>
        get() = _searchGiftsStatus

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    private val _onAlterMsgShowing = MutableLiveData<GiftPost>()
    val onAlterMsgShowing: LiveData<GiftPost>
        get() = _onAlterMsgShowing

    private val _onCommentsShowing = MutableLiveData<GiftPost?>()
    val onCommentsShowing: LiveData<GiftPost?>
        get() = _onCommentsShowing

    private val _allGifts = MutableLiveData<List<GiftPost>>()
    val allGifts: LiveData<List<GiftPost>>
        get() = _allGifts

    private val _abandonStatus = MutableLiveData<LoadApiStatus>()
    val abandonStatus: LiveData<LoadApiStatus>
        get() = _abandonStatus


    var gifts = listOf<GiftPost>()

    init {
        onSearchGiftsDetail()
    }

    private fun onSearchGiftsDetail() {

        coroutineScope.launch {
            _searchGiftsStatus.value = LoadApiStatus.LOADING

            when (val result = repository.getUserHistoryPosts(
                collection = PATH_GIFT_POST,
                uid = userInfo!!.uid)) {

                is Result.Success -> {
                    _error.value = null

                    gifts = result.data

                    _searchGiftsStatus.value = LoadApiStatus.DONE

                }
                is Result.Fail -> {
                    _error.value = result.error
                    _searchGiftsStatus.value = LoadApiStatus.ERROR

                    _allGifts.value = emptyList()
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _searchGiftsStatus.value = LoadApiStatus.ERROR

                    _allGifts.value = emptyList()
                }
                else -> {
                    _error.value =
                        WeShareApplication.instance.getString(R.string.result_fail)
                    _searchGiftsStatus.value = LoadApiStatus.ERROR

                    _allGifts.value = emptyList()
                }
            }
        }
    }


    fun filteringGift(index: Int) {
        when (index) {

            0 -> _allGifts.value = gifts

            1 -> {
                _allGifts.value = gifts.filter { it.status == GiftStatusType.OPENING.code }
            }

            2 -> {
                _allGifts.value = gifts.filter { it.status == GiftStatusType.CLOSED.code }
            }

            3 -> {
                _allGifts.value = gifts.filter { it.status == GiftStatusType.ABANDONED.code }
            }
        }
    }

    fun userCheckWhoRequest(gift: GiftPost) {
        _onCommentsShowing.value = gift
    }

    fun showCommentsComplete() {
        _onCommentsShowing.value = null
    }

    fun userPressAbandon(gift: GiftPost) {
        _onAlterMsgShowing.value = gift
    }

    fun abandonGift(selectedGift: GiftPost) {
        coroutineScope.launch {
            _abandonStatus.value = LoadApiStatus.LOADING

            when (val result =
                repository.updateGiftStatus(selectedGift.id, GiftStatusType.ABANDONED.code, "")) {
                is Result.Success -> {
                    _error.value = null
                    _abandonStatus.value = LoadApiStatus.DONE
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _abandonStatus.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _abandonStatus.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value =
                        WeShareApplication.instance.getString(R.string.result_fail)
                    _abandonStatus.value = LoadApiStatus.ERROR
                }
            }
        }
    }
}

