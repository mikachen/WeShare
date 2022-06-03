package com.zoe.weshare.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.ext.toDisplayDateFormat
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.Const.FIELD_WHO_LIKED
import com.zoe.weshare.util.Const.PATH_EVENT_POST
import com.zoe.weshare.util.Const.PATH_GIFT_POST
import com.zoe.weshare.util.EventStatusType
import com.zoe.weshare.util.GiftStatusType
import com.zoe.weshare.util.UserManager.userBlackList
import com.zoe.weshare.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

const val GIFT_CARD = 0
const val EVENT_CARD = 1

class MapViewModel(
    private val repository: WeShareRepository, val userInfo: UserInfo) : ViewModel() {

    private var _gifts = MutableLiveData<List<GiftPost>>()
    val gifts: LiveData<List<GiftPost>>
        get() = _gifts

    private var _events = MutableLiveData<List<EventPost>>()
    val events: LiveData<List<EventPost>>
        get() = _events

    private var _cards = MutableLiveData<List<Cards>>()
    val cards: LiveData<List<Cards>>
        get() = _cards

    private val _snapPosition = MutableLiveData<Int>()
    val snapPosition: LiveData<Int>
        get() = _snapPosition

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    private val _navigateToSelectedGift = MutableLiveData<GiftPost?>()
    val navigateToSelectedGift: LiveData<GiftPost?>
        get() = _navigateToSelectedGift

    private val _navigateToSelectedEvent = MutableLiveData<EventPost?>()
    val navigateToSelectedEvent: LiveData<EventPost?>
        get() = _navigateToSelectedEvent

    private val cardsViewList = mutableListOf<Cards>()

    var hasGiftCardsCreated: Boolean = false
    var hasEventCardsCreated: Boolean = false

    init {
        getGiftsResult()
        getEventsResult()
    }

    private fun getGiftsResult() {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING
            val result = repository.getAllGifts()

            _gifts.value = when (result) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    val allGifts = result.data
                    getFilteredGifts(allGifts)
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
        }
    }

    /**
     * exclude the authors in user's black list, closed gifts
     * */
    private fun getFilteredGifts(allGifts: List<GiftPost>): List<GiftPost> {
        return allGifts.filterNot {
            userBlackList.contains(it.author.uid) || it.status == GiftStatusType.CLOSED.code
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

                    val allEvents = result.data
                    getFilteredEvents(allEvents)
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
        }
    }

    /**
     * exclude the authors in user's black list, closed gifts
     * */
    private fun getFilteredEvents(allEvents: List<EventPost>): List<EventPost> {
        return allEvents.filterNot {
            userBlackList.contains(it.author.uid) &&
                    it.status == EventStatusType.WAITING.code
        }
    }

    fun onGiftCardPrepare(gifts: List<GiftPost>) {
        if (gifts.isNotEmpty()) {
            for (item in gifts) {
                val newCard = Cards(
                    id = item.id,
                    title = item.title,
                    description = item.description,
                    createdTime = item.createdTime,
                    eventTime = "",
                    postType = GIFT_CARD,
                    image = item.image,
                    postLocation = item.location,
                    whoLiked = item.whoLiked
                )
                cardsViewList.add(newCard)
            }
            hasGiftCardsCreated = true
        }
        _cards.value = cardsViewList.shuffled()
    }

    fun onEventCardPrepare(events: List<EventPost>) {
        if (events.isNotEmpty()) {
            for (item in events) {
                val newCard = Cards(
                    id = item.id,
                    title = item.title,
                    description = item.description,
                    createdTime = item.createdTime,
                    eventTime = WeShareApplication.instance.getString(
                        R.string.preview_event_time,
                        item.startTime.toDisplayDateFormat(),
                        item.endTime.toDisplayDateFormat()
                    ),
                    postType = EVENT_CARD,
                    image = item.image,
                    postLocation = item.location,
                    whoLiked = item.whoLiked
                )
                cardsViewList.add(newCard)
            }
            hasEventCardsCreated = true
        }
        _cards.value = cardsViewList.shuffled()
    }

    fun onPostLikePressed(card: Cards, hasUserLiked: Boolean) {
        val collection =
            when (card.postType) {
                GIFT_CARD -> PATH_GIFT_POST
                EVENT_CARD -> PATH_EVENT_POST
                else -> {
                    ""
                }
            }

        if (!hasUserLiked) {
            sendLike(collection, card.id)
        } else {
            cancelLike(collection, card.id)
        }
    }

    private fun sendLike(collection: String, doc: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.updateFieldValue(
                    collection = collection,
                    docId = doc,
                    field = FIELD_WHO_LIKED,
                    value = FieldValue.arrayUnion(userInfo.uid)
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
                    field = FIELD_WHO_LIKED,
                    value = FieldValue.arrayRemove(userInfo.uid)
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

    fun onNavigateToDetail(card: Cards) {
        when (card.postType) {
            GIFT_CARD -> _navigateToSelectedGift.value = getGiftResult(card)
            EVENT_CARD -> _navigateToSelectedEvent.value = getEventResult(card)
        }
    }

    private fun getGiftResult(card: Cards): GiftPost? {
        return _gifts.value?.find { it.id == card.id }
    }


    private fun getEventResult(card: Cards): EventPost? {
        return _events.value?.find { it.id == card.id }
    }

    fun displayCardDetailsComplete() {
        _navigateToSelectedGift.value = null
        _navigateToSelectedEvent.value = null
    }


    /**
     * observe card recycleView position for map marker window reveal use
     * */
    fun onGalleryScrollChange(
        layoutManager: RecyclerView.LayoutManager?,
        linearSnapHelper: LinearSnapHelper,
    ) {
        val snapView = linearSnapHelper.findSnapView(layoutManager)

        snapView?.let {
            layoutManager?.getPosition(snapView)?.let {
                if (it != snapPosition.value) {

                    cards.value?.let { cards ->
                        _snapPosition.value = it % cards.size
                    }
                }
            }
        }
    }
}
