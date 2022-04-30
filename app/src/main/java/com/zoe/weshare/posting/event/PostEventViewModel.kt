package com.zoe.weshare.posting.event

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.ext.toDisplayFormat
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.ChatRoomType
import com.zoe.weshare.util.LogType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PostEventViewModel(private val repository: WeShareRepository, private val author: UserInfo?) :
    ViewModel() {

    var postingProgress = MutableLiveData<Int>()

    val _event = MutableLiveData<EventPost?>()
    val event: LiveData<EventPost?>
        get() = _event

    var imageUri = MutableLiveData<Uri?>()
    var locationChoice: PostLocation? = null

    var onPostEvent = MutableLiveData<EventPost>()

    var startTime: Long = -1
    var endTime: Long = -1

    private val _datePick = MutableLiveData<String>()
    val datePick: LiveData<String>
        get() = _datePick

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _postEventStatus = MutableLiveData<LoadApiStatus>()
    val postEventStatus: LiveData<LoadApiStatus>
        get() = _postEventStatus

    private val _roomCreateComplete = MutableLiveData<String>()
    val roomCreateComplete: LiveData<String>
        get() = _roomCreateComplete

    private val _saveLogComplete = MutableLiveData<LoadApiStatus>()
    val saveLogComplete: LiveData<LoadApiStatus>
        get() = _saveLogComplete

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    fun onNewEventPost(roomId: String) {
        _event.value!!.roomId = roomId
        newEventPost()
    }

    private fun newEventPost() {
        coroutineScope.launch {
            _postEventStatus.value = LoadApiStatus.LOADING

            postingProgress.value = 70
            when (val result = event.value?.let { repository.postNewEvent(it) }) {
                is Result.Success -> {
                    _error.value = null
                    _postEventStatus.value = LoadApiStatus.DONE

                    onSaveEventPostLog(result.data)

                    postingProgress.value = 80
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
            logType = LogType.POST_EVENT.value,
            operatorUid = author!!.uid,
            logMsg = WeShareApplication.instance.getString(
                R.string.log_msg_post_event,
                author.name,
                event.value!!.title
            )
        )
        saveEventPostLog(log)
    }

    private fun saveEventPostLog(log: PostLog) {
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
        _event.value!!.location = locationChoice
    }

    fun uploadImage() {
        coroutineScope.launch {
            _postEventStatus.value = LoadApiStatus.LOADING

            postingProgress.value = 10

            val imageUri = Uri.parse(event.value!!.image)
            when (val result = repository.uploadImage(imageUri)) {
                is Result.Success -> {
                    _error.value = null
                    _postEventStatus.value = LoadApiStatus.DONE

                    _event.value!!.image = result.data

                    onPostEvent.value = event.value

                    postingProgress.value = 30
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

    fun onNewRoomPrepare() {
        val eventRoom = ChatRoom(
            type = ChatRoomType.MULTIPLE.value,
        )
        createEventRoom(eventRoom)
    }

    private fun createEventRoom(room: ChatRoom) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            postingProgress.value = 50

            when (val result = repository.createNewChatRoom(room)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    _roomCreateComplete.value = result.data ?: ""

                    postingProgress.value = 60
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

    fun onDatePickDisplay(startDate: Long, secondDate: Long) {
        startTime = startDate
        endTime = secondDate

        _datePick.value = WeShareApplication.instance.getString(R.string.preview_event_time,
            startTime.toDisplayFormat(),
            endTime.toDisplayFormat())
    }

    fun onSaveUserInput(
        title: String,
        sort: String,
        volunteerNeeds: String,
        description: String,
    ) {
        onPostEvent.value = EventPost(
            author = author,
            title = title,
            sort = sort,
            volunteerNeeds = volunteerNeeds.toInt(),
            description = description,
            image = imageUri.value.toString(),
            startTime = startTime,
            endTime = endTime
        )
        _event.value = onPostEvent.value
    }

    fun navigateNextComplete() {
        _event.value = null
    }
}
