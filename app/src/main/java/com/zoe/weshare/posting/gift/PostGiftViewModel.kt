package com.zoe.weshare.posting.gift

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.LogType
import com.zoe.weshare.util.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PostGiftViewModel(
    private val repository: WeShareRepository,
    private val author: UserInfo?,
) : ViewModel() {

    var _gift = MutableLiveData<GiftPost>()
    val gift: LiveData<GiftPost>
        get() = _gift

    var imageUri : String = ""

    val locationChoice = MutableLiveData<LatLng>()

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // status: The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _saveLogComplete = MutableLiveData<PostLog>()
    val saveLogComplete: LiveData<PostLog>
        get() = _saveLogComplete

    // error: The internal MutableLiveData that stores the error of the most recent request
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    fun newGiftPost(gift: GiftPost) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (val result = repository.postNewGift(gift)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    //save log only when Status.DONE
                    onSaveGiftPostLog(gift, docId = result.data)
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
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }


    private fun onSaveGiftPostLog(gift: GiftPost, docId: String) {
        val log = PostLog(
            id = docId,
            title = gift.title,
            type = LogType.GIFT.value
        )
        saveGiftPostLog(log,author!!.uid)
    }

    private fun saveGiftPostLog(log: PostLog, uid: String) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (val result = repository.saveGiftPostLog(log, uid)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    _saveLogComplete.value = log
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
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    // fragment view binding edit text pass in data
    fun updateLocation(locationName: String, point: LatLng) {
        _gift.value?.apply {

            location = PostLocation(
                locationName = locationName,
                latitude = point.latitude.toString(),
                longitude = point.longitude.toString()
            )
        }
    }

    fun onSaveUserInput(title: String, sort: String, condition: String, description: String) {
        _gift.value = GiftPost(
            author = author,
            title = title,
            sort = sort,
            condition = condition,
            image = imageUri,
            description = description
        )
    }
}
