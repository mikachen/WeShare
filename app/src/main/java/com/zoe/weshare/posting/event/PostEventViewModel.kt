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
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.ChatRoomType
import com.zoe.weshare.util.LogType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PostEventViewModel(private val repository: WeShareRepository, private val author: UserInfo?) :
    ViewModel() {

    var postingProgress = MutableLiveData<Int?>()

    var onPostEvent = MutableLiveData<EventPost?>()

    var locationChoice: PostLocation? = null

    private val _event = MutableLiveData<EventPost?>()
    val event: LiveData<EventPost?>
        get() = _event

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _postEventStatus = MutableLiveData<LoadApiStatus>()
    val postEventStatus: LiveData<LoadApiStatus>
        get() = _postEventStatus

    private val _saveLogComplete = MutableLiveData<OperationLog?>()
    val saveLogComplete: LiveData<OperationLog?>
        get() = _saveLogComplete

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status


    fun fetchArgument(event: EventPost) {
        _event.value = event
    }

    /**
     * (A) upload image
     * reassign event.image to firebase url string result
     * */
    fun uploadImage() {
        coroutineScope.launch {
            _postEventStatus.value = LoadApiStatus.LOADING
            postingProgress.value = 10

            val imageUri = Uri.parse(event.value!!.image)

            when (val result = repository.uploadImage(imageUri)) {
                is Result.Success -> {
                    _error.value = null
                    postingProgress.value = 30
                    _postEventStatus.value = LoadApiStatus.DONE

                    val firebaseUrl = result.data

                    _event.value!!.image = firebaseUrl
                    onPostEvent.value = event.value
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

    /**
     * (B) prepare and create multiple chatroom for event
     *  reassign event roomId when done
     * */
    fun onNewRoomPrepare(event: EventPost) {
        val eventRoom = ChatRoom(
            type = ChatRoomType.MULTIPLE.value,
            eventTitle = event.title,
            eventImage = event.image
        )
        createEventRoom(eventRoom)
    }


    /**
     * (C) reassign event roomId when done
     * */
    private fun createEventRoom(room: ChatRoom) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            postingProgress.value = 50

            when (val result = repository.createNewChatRoom(room)) {
                is Result.Success -> {
                    _error.value = null
                    postingProgress.value = 60
                    _status.value = LoadApiStatus.DONE

                    val chatRoomId = result.data
                    _event.value!!.roomId = chatRoomId

                    newEventPost()
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
     * (D) Post Event to firebase
     * */
    private fun newEventPost() {
        coroutineScope.launch {
            _postEventStatus.value = LoadApiStatus.LOADING

            postingProgress.value = 70

            when (val result = event.value?.let { repository.postNewEvent(it) }) {
                is Result.Success -> {
                    _error.value = null
                    postingProgress.value = 80
                    _postEventStatus.value = LoadApiStatus.DONE

                    val eventDocId = result.data

                    onSaveEventPostLog(eventDocId)
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


    /**
     * (E) prepare and post user's operation log
     * */
    private fun onSaveEventPostLog(docId: String) {
        val log = OperationLog(
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

    private fun saveEventPostLog(log: OperationLog) {
        coroutineScope.launch {
            postingProgress.value = 90

            when (val result = repository.saveLog(log)) {
                is Result.Success -> {
                    _error.value = null
                    postingProgress.value = 100

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
        _event.value!!.location = locationChoice
    }


    fun fetchUserInput(
        title: String,
        sort: String,
        volunteerNeeds: String,
        description: String,
        imageUri: Uri,
        startTime: Long,
        endTime: Long,
    ) {
        onPostEvent.value = EventPost(
            author = author,
            title = title,
            sort = sort,
            volunteerNeeds = volunteerNeeds.toInt(),
            description = description,
            image = imageUri.toString(),
            startTime = startTime,
            endTime = endTime
        )
        _event.value = onPostEvent.value
    }

    fun navigateNextComplete() {
        _event.value = null
    }

    fun postEventComplete() {
        postingProgress.value = null
        _event.value = null
        onPostEvent.value = null
        _saveLogComplete.value = null
    }
}
