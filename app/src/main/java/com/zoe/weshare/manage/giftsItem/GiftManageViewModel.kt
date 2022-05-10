package com.zoe.weshare.manage.giftsItem

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
import com.zoe.weshare.util.LogType
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

    private val _allGiftsResult = MutableLiveData<List<GiftPost>>()
    val allGiftsResult: LiveData<List<GiftPost>>
        get() = _allGiftsResult

    var onFilterEmpty = MutableLiveData<Boolean>()

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

    private val _abandonStatus = MutableLiveData<LoadApiStatus>()
    val abandonStatus: LiveData<LoadApiStatus>
        get() = _abandonStatus

    private val _saveLogComplete = MutableLiveData<LoadApiStatus>()
    val saveLogComplete: LiveData<LoadApiStatus>
        get() = _saveLogComplete

    init {
        getUserAllGiftsPosts()
    }

    fun getUserAllGiftsPosts() {
        coroutineScope.launch {
            _searchGiftsStatus.value = LoadApiStatus.LOADING

            when (
                val result = repository.getUserAllGiftsPosts(uid = userInfo!!.uid)
            ) {

                is Result.Success -> {
                    _error.value = null
                    _searchGiftsStatus.value = LoadApiStatus.DONE

                    _allGiftsResult.value = result.data?: emptyList()
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _searchGiftsStatus.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _searchGiftsStatus.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value =
                        WeShareApplication.instance.getString(R.string.result_fail)
                    _searchGiftsStatus.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun onNaviagteToRequest(gift: GiftPost) {
        _onCommentsShowing.value = gift
    }

    fun naviagteToRequestComplete() {
        _onCommentsShowing.value = null
    }

    fun userClickAbandon(gift: GiftPost) {
        _onAlterMsgShowing.value = gift
    }

    fun abandonGift(selectedGift: GiftPost) {
        coroutineScope.launch {
            _abandonStatus.value = LoadApiStatus.LOADING

            when (
                val result =
                    repository.updateGiftStatus(selectedGift.id, GiftStatusType.ABANDONED.code, "")
            ) {
                is Result.Success -> {
                    _error.value = null
                    _abandonStatus.value = LoadApiStatus.DONE

                    onSaveAbandonGiftLog(selectedGift)
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

    fun onSaveAbandonGiftLog(gift: GiftPost) {
        val log = OperationLog(
            postDocId = gift.id,
            logType = LogType.ABANDONED_GIFT.value,
            operatorUid = userInfo!!.uid,
            logMsg = WeShareApplication.instance.getString(
                R.string.log_msg_abandon_gift,
                userInfo.name,
                gift.title
            )
        )
        saveAbandonLog(log)
    }

    private fun saveAbandonLog(log: OperationLog) {
        coroutineScope.launch {
            _saveLogComplete.value = LoadApiStatus.LOADING

            when (
                val result =
                    repository.saveLog(log)
            ) {
                is Result.Success -> {
                    _saveLogComplete.value = LoadApiStatus.DONE
                    _error.value = null
                }
                is Result.Fail -> {
                    _saveLogComplete.value = LoadApiStatus.ERROR
                    _error.value = result.error
                }
                is Result.Error -> {
                    _saveLogComplete.value = LoadApiStatus.ERROR
                    _error.value = result.exception.toString()
                }
                else -> {
                    _saveLogComplete.value = LoadApiStatus.ERROR
                    _error.value =
                        WeShareApplication.instance.getString(R.string.result_fail)
                }
            }
        }
    }

    fun refreshFilterView() {
        getUserAllGiftsPosts()
    }
}
