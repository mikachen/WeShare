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

    private val _postEventStatus = MutableLiveData<LoadApiStatus>()
    val postEventStatus: LiveData<LoadApiStatus>
        get() = _postEventStatus

    private val _postEventComplete = MutableLiveData<String>()
    val postEventComplete: LiveData<String>
        get() = _postEventComplete

    private val _saveLogComplete = MutableLiveData<LoadApiStatus>()
    val saveLogComplete: LiveData<LoadApiStatus>
        get() = _saveLogComplete

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    fun newEventPost(event: EventPost) {
        coroutineScope.launch {

            _postEventStatus.value = LoadApiStatus.LOADING

            when (val result = repository.postNewEvent(event)) {
                is Result.Success -> {
                    _error.value = null
                    _postEventStatus.value = LoadApiStatus.DONE

                    _postEventComplete.value = result.data!!
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _postEventStatus.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _postEventStatus.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _postEventStatus.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun onSaveEventPostLog(docId: String) {
        val log = PostLog(
            postDocId = docId,
            logType = LogType.POSTEVENT.value,
            operatorUid = author!!.uid,
            logMsg = WeShareApplication.instance.getString(R.string.log_msg_post_event,
                author.name,
                event.value?.title ?: "")
        )
        saveEventPostLog(log)
    }

    private fun saveEventPostLog(log: PostLog) {
        coroutineScope.launch {

            _saveLogComplete.value = LoadApiStatus.LOADING

            when (val result = repository.saveLog(log)) {
                is Result.Success -> {
                    _error.value = null
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
