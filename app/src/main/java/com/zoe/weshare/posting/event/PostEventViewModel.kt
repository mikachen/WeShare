package com.zoe.weshare.posting.event

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

class PostEventViewModel(private val repository: WeShareRepository, private val author: UserInfo?) :
    ViewModel() {

    val _event = MutableLiveData<EventPost>()
    val event: LiveData<EventPost>
        get() = _event

    val locationChoice = MutableLiveData<LatLng>()


    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _saveLogComplete = MutableLiveData<PostLog>()
    val saveLogComplete: LiveData<PostLog>
        get() = _saveLogComplete

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    fun newEventPost(event: EventPost) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (val result = repository.postNewEvent(event)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    //save log only when Status.DONE
//                    onSaveEventPostLog(event, docId = result.data)
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

    private fun onSaveEventPostLog(event: EventPost, docId: String) {
        val log = PostLog(
            id = docId,
            title = event.title,
            type = LogType.GIFT.value
        )
        saveEventPostLog(log,author!!.uid)
    }

    private fun saveEventPostLog(log: PostLog, uid: String) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (val result = repository.saveEventPostLog(log, uid)) {
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


    // update user's location choice
    fun updateLocation(locationName: String, point: LatLng) {
        _event.value?.apply {

            location = PostLocation(
                locationName = locationName,
                latitude = point.latitude.toString(),
                longitude = point.longitude.toString()
            )
        }
    }

}
