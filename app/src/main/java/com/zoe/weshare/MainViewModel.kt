package com.zoe.weshare

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.data.ChatRoom
import com.zoe.weshare.data.OperationLog
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.util.CurrentFragmentType
import com.zoe.weshare.util.UserManager.weShareUser

class MainViewModel(private val repository: WeShareRepository) : ViewModel() {

    val currentFragmentType = MutableLiveData<CurrentFragmentType>()

    var liveNotifications = MutableLiveData<List<OperationLog>>()
    var liveChatRooms = MutableLiveData<List<ChatRoom>>()

    var reObserveNotification = MutableLiveData<Boolean>()
    var reObserveChatRoom = MutableLiveData<Boolean>()

    var roomBadgeCount = MutableLiveData<Int>()


    fun getLiveNotificationResult() {
        liveNotifications = repository.getLiveNotifications(weShareUser!!.uid)
        reObserveNotification.value = true
    }

    fun getLiveRoomResult() {
        liveChatRooms = repository.getLiveRoomLists(weShareUser!!.uid)
        reObserveChatRoom.value = true
    }

    fun getUnreadRoom(rooms: List<ChatRoom>) {

        var unReadRoom = 0

        for (room in rooms) {
            if (!room.lastMsgRead.contains(weShareUser!!.uid)) {
                unReadRoom += 1
            }
        }
        roomBadgeCount.value = unReadRoom
    }
}