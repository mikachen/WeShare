package com.zoe.weshare.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.firestore.FieldValue
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.ext.toDisplayDateFormat
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.*
import com.zoe.weshare.util.Const.PATH_EVENT_POST
import com.zoe.weshare.util.Const.PATH_GIFT_POST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

const val GIFT_CARD = 0
const val EVENT_CARD = 1

class MapViewModel(private val repository: WeShareRepository, val userInfo: UserInfo?) :
    ViewModel() {

    var googleMap: GoogleMap? = null
    var isPermissionGranted: Boolean = false

    private var _gifts = MutableLiveData<List<GiftPost>>()
    val gifts: LiveData<List<GiftPost>>
        get() = _gifts

    private var _events = MutableLiveData<List<EventPost>>()
    val events: LiveData<List<EventPost>>
        get() = _events

    private var _cards = MutableLiveData<List<Cards>>()
    val cards: LiveData<List<Cards>>
        get() = _cards

    // it for map marker choise
    private val _snapPosition = MutableLiveData<Int>()
    val snapPosition: LiveData<Int>
        get() = _snapPosition

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // status: The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    // error: The internal MutableLiveData that stores the error of the most recent request
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    // status for the loading icon of swl
    private val _refreshStatus = MutableLiveData<Boolean>()
    val refreshStatus: LiveData<Boolean>
        get() = _refreshStatus

    private val _navigateToSelectedGift = MutableLiveData<GiftPost?>()
    val navigateToSelectedGift: LiveData<GiftPost?>
        get() = _navigateToSelectedGift

    private val _navigateToSelectedEvent = MutableLiveData<EventPost?>()
    val navigateToSelectedEvent: LiveData<EventPost?>
        get() = _navigateToSelectedEvent

    val cardsViewList = mutableListOf<Cards>()

    var isGiftCardsComplete: Boolean = false
    var isEventCardsComplete: Boolean = false

    init {
        getGiftsResult()
        getEventsResult()
    }

    fun onCardPrepare(gifts: List<GiftPost>?, events: List<EventPost>?) {

        if (gifts != null) {
            for (element in gifts) {
                val newCard = Cards(
                    id = element.id,
                    title = element.title,
                    description = element.description,
                    createdTime = element.createdTime,
                    eventTime = "",
                    postType = GIFT_CARD,
                    image = element.image,
                    postLocation = element.location,
                    whoLiked = element.whoLiked
                )
                cardsViewList.add(newCard)
            }
            isGiftCardsComplete = true
        }
        if (events != null) {
            for (element in events) {
                val newCard = Cards(
                    id = element.id,
                    title = element.title,
                    description = element.description,
                    createdTime = element.createdTime,
                    eventTime = WeShareApplication.instance.getString(
                        R.string.preview_event_time,
                        element.startTime.toDisplayDateFormat(),
                        element.endTime.toDisplayDateFormat()
                    ),
                    postType = EVENT_CARD,
                    image = element.image,
                    postLocation = element.location,
                    whoLiked = element.whoLiked
                )
                cardsViewList.add(newCard)
            }
            isEventCardsComplete = true
        }
        _cards.value = cardsViewList.shuffled()
    }

    private fun getGiftsResult() {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING
            val result = repository.getAllGifts()

            _gifts.value = when (result) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    result.data.filterNot {
                        !UserManager.userBlackList.contains(it.author!!.uid) &&
                        it.status == GiftStatusType.CLOSED.code }
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
            _refreshStatus.value = false
        }
    }

    private fun getEventsResult() {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING
            val result = repository.getAllEvents()

            _events.value = when (result) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE
                    result.data.filterNot {
                        !UserManager.userBlackList.contains(it.author!!.uid) &&
                        it.status == EventStatusType.WAITING.code }
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
            _refreshStatus.value = false
        }
    }

    fun onPostLikePressed(card: Cards, isUserLiked: Boolean) {
        val collection = when (card.postType) {
            GIFT_CARD -> PATH_GIFT_POST
            EVENT_CARD -> PATH_EVENT_POST
            else -> {
                "unknow"
            }
        }

        val docId = card.id

        if (!isUserLiked) {
            sendLike(collection, docId)
        } else {
            cancelLike(collection, docId)
        }
    }

    private fun sendLike(collection: String, doc: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.updateFieldValue(
                    collection = collection,
                    docId = doc,
                    field = Const.FIELD_WHO_LIKED,
                    value = FieldValue.arrayUnion(userInfo!!.uid)
                )
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE
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
                    _error.value = Util.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    private fun cancelLike(collection: String, doc: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.updateFieldValue(
                    collection = collection,
                    docId = doc,
                    field = Const.FIELD_WHO_LIKED,
                    value = FieldValue.arrayRemove(userInfo!!.uid)
                )
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE
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
                    _error.value = Util.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun displayCardDetails(card: Cards) {
        when (card.postType) {
            0 -> _navigateToSelectedGift.value = _gifts.value?.single { it.id == card.id }
            1 -> _navigateToSelectedEvent.value = _events.value?.single { it.id == card.id }
        }
    }

    fun displayCardDetailsComplete() {
        _navigateToSelectedGift.value = null
        _navigateToSelectedEvent.value = null
    }

    fun onGalleryScrollChange(
        layoutManager: RecyclerView.LayoutManager?,
        linearSnapHelper: LinearSnapHelper,
    ) {

        val snapView = linearSnapHelper.findSnapView(layoutManager)
        snapView?.let {
            layoutManager?.getPosition(snapView)?.let {
                if (it != snapPosition.value) {
                    _snapPosition.value = it % cards.value!!.size
                }
            }
        }
    }

    fun saveMapInfo(map: GoogleMap) {
        googleMap = map
    }
}
