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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PostGiftViewModel(
    private val repository: WeShareRepository,
    private val author: UserInfo?,
) : ViewModel() {

    var postingProgress = MutableLiveData<Int?>()

    var onPostGift = MutableLiveData<GiftPost?>()

    var locationChoice: PostLocation? = null

    private var _gift = MutableLiveData<GiftPost?>()
    val gift: LiveData<GiftPost?>
        get() = _gift

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _postGiftStatus = MutableLiveData<LoadApiStatus>()
    val postGiftStatus: LiveData<LoadApiStatus>
        get() = _postGiftStatus

    private val _saveLogComplete = MutableLiveData<OperationLog?>()
    val saveLogComplete: LiveData<OperationLog?>
        get() = _saveLogComplete

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    fun fetchArgument(gift: GiftPost) {
        _gift.value = gift
    }

    /**
     * (A) upload image
     * reassign gift.image to firebase url string result
     * */
    fun uploadImage() {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING
            postingProgress.value = 10

            val imageUri = Uri.parse(gift.value!!.image)

            when (val result = repository.uploadImage(imageUri)) {
                is Result.Success -> {
                    postingProgress.value = 30
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    val firebaseUrl = result.data

                    _gift.value!!.image = firebaseUrl
                    onPostGift.value = gift.value
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

    /**
     * (B) Post Gift to firebase
     * */
    fun newGiftPost(gift: GiftPost) {
        coroutineScope.launch {
            _postGiftStatus.value = LoadApiStatus.LOADING

            postingProgress.value = 50

            when (val result = repository.postNewGift(gift)) {
                is Result.Success -> {
                    postingProgress.value = 80
                    _error.value = null
                    _postGiftStatus.value = LoadApiStatus.DONE

                    val giftDocId = result.data


                    onSaveGiftPostLog(giftDocId)
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _postGiftStatus.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _postGiftStatus.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _postGiftStatus.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    /**
     * (C) prepare and post user's operation log
     * */

    fun onSaveGiftPostLog(docId: String) {
        val log = OperationLog(
            postDocId = docId,
            logType = LogType.POST_GIFT.value,
            operatorUid = author!!.uid,
            logMsg = WeShareApplication.instance.getString(
                R.string.log_msg_post_gift,
                author.name,
                gift.value!!.title
            )
        )
        saveGiftPostLog(log)
    }

    private fun saveGiftPostLog(log: OperationLog) {
        coroutineScope.launch {

            postingProgress.value = 90

            when (val result = repository.saveLog(log)) {
                is Result.Success -> {
                    postingProgress.value = 100
                    _error.value = null

                    _saveLogComplete.value = log
                }
                is Result.Fail -> {
                    _error.value = result.error
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                }
                else -> {
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                }
            }
        }
    }

    fun updateLocation(locationName: String, point: LatLng) {
        locationChoice = PostLocation(
            locationName = locationName,
            latitude = point.latitude.toString(),
            longitude = point.longitude.toString()
        )
        _gift.value!!.location = locationChoice
    }

    fun onSaveUserInput(
        title: String,
        sort: String,
        condition: String,
        description: String,
        imageUri: Uri,
    ) {
        onPostGift.value = GiftPost(
            author = author,
            title = title,
            sort = sort,
            condition = condition,
            image = imageUri.toString(),
            description = description
        )
        _gift.value = onPostGift.value
    }

    fun navigateNextComplete() {
        _gift.value = null
    }

    fun postGiftComplete() {
        postingProgress.value = null
        _gift.value = null
        onPostGift.value = null
        _saveLogComplete.value = null
    }
}
