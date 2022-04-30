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

    var postingProgress = MutableLiveData<Int>()

    var _gift = MutableLiveData<GiftPost>()
    val gift: LiveData<GiftPost>
        get() = _gift

    var onPostGift = MutableLiveData<GiftPost>()

    var imageUri: Uri? = null
    var locationChoice: PostLocation? = null


    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _postGiftStatus = MutableLiveData<LoadApiStatus>()
    val postGiftStatus: LiveData<LoadApiStatus>
        get() = _postGiftStatus

    private val _saveLogComplete = MutableLiveData<LoadApiStatus>()
    val saveLogComplete: LiveData<LoadApiStatus>
        get() = _saveLogComplete

    // error: The internal MutableLiveData that stores the error of the most recent request
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    private val _postingStatus = MutableLiveData<LoadApiStatus>()
    val postingStatus: LiveData<LoadApiStatus>
        get() = _postingStatus

    fun newGiftPost(gift: GiftPost) {
        coroutineScope.launch {
            _postGiftStatus.value = LoadApiStatus.LOADING

            postingProgress.value = 50

            when (val result = repository.postNewGift(gift)) {
                is Result.Success -> {
                    _error.value = null
                    _postGiftStatus.value = LoadApiStatus.DONE

                    onSaveGiftPostLog(result.data)
                    postingProgress.value = 80
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

    fun onSaveGiftPostLog(docId: String) {
        val log = PostLog(
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

    private fun saveGiftPostLog(log: PostLog) {
        coroutineScope.launch {
            _saveLogComplete.value = LoadApiStatus.LOADING

            postingProgress.value = 90

            when (val result = repository.saveLog(log)) {
                is Result.Success -> {
                    _error.value = null

                    postingProgress.value = 100
                    _saveLogComplete.value = LoadApiStatus.DONE
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _saveLogComplete.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _saveLogComplete.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _saveLogComplete.value = LoadApiStatus.ERROR
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

    fun onSaveUserInput(title: String, sort: String, condition: String, description: String) {
        _gift.value = GiftPost(
            author = author,
            title = title,
            sort = sort,
            condition = condition,
            image = imageUri.toString(),
            description = description
        )
    }

    fun uploadImage() {
        coroutineScope.launch {
            _postingStatus.value = LoadApiStatus.LOADING

            postingProgress.value = 10

            val imageUri = Uri.parse(gift.value!!.image)
            when (val result = repository.uploadImage(imageUri)) {
                is Result.Success -> {
                    _error.value = null

                    _gift.value!!.image = result.data

                    onPostGift.value = gift.value

                    postingProgress.value = 30
                    _postingStatus.value = LoadApiStatus.DONE
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _postingStatus.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _postingStatus.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _postingStatus.value = LoadApiStatus.ERROR
                }
            }
        }
    }
}
